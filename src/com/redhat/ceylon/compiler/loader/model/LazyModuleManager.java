/*
 * Copyright Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the authors tag. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License version 2.
 * 
 * This particular file is subject to the "Classpath" exception as provided in the 
 * LICENSE file that accompanied this code.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package com.redhat.ceylon.compiler.loader.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.redhat.ceylon.cmr.api.ArtifactContext;
import com.redhat.ceylon.cmr.api.ArtifactResult;
import com.redhat.ceylon.cmr.api.ImportType;
import com.redhat.ceylon.cmr.api.JDKUtils;
import com.redhat.ceylon.cmr.api.ModuleDependencyInfo;
import com.redhat.ceylon.cmr.api.ModuleInfo;
import com.redhat.ceylon.cmr.api.Overrides;
import com.redhat.ceylon.cmr.api.VersionComparator;
import com.redhat.ceylon.common.Backend;
import com.redhat.ceylon.common.ModuleUtil;
import com.redhat.ceylon.common.Versions;
import com.redhat.ceylon.compiler.loader.AbstractModelLoader;
import com.redhat.ceylon.compiler.typechecker.analyzer.ModuleManager;
import com.redhat.ceylon.compiler.typechecker.analyzer.Warning;
import com.redhat.ceylon.compiler.typechecker.context.Context;
import com.redhat.ceylon.compiler.typechecker.context.PhasedUnits;
import com.redhat.ceylon.compiler.typechecker.model.Module;
import com.redhat.ceylon.compiler.typechecker.model.ModuleImport;
import com.redhat.ceylon.compiler.typechecker.tree.Node;

/**
 * ModuleManager which can load artifacts from jars and cars.
 *
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public abstract class LazyModuleManager extends ModuleManager {

    public LazyModuleManager(Context ceylonContext) {
        super(ceylonContext);
    }

    protected void setupIfJDKModule(LazyModule module) {
        // Make sure that the java modules are set up properly.
        // Bad jdk versions will not be made available and the module validator
        // will fail to load their artifacts, and the error is properly handled by the lazy module manager in
        // attachErrorToDependencyDeclaration()
        String nameAsString = module.getNameAsString();
        String version = module.getVersion();
        if(version != null
                && AbstractModelLoader.isJDKModule(nameAsString)){
            if(JDKUtils.jdk.providesVersion(version)){
                module.setAvailable(true);
                module.setJava(true);
                module.setNative(Backend.Java.nativeAnnotation);
            }
        }
    }

    @Override
    public void resolveModule(ArtifactResult artifact, Module module, ModuleImport moduleImport, 
            LinkedList<Module> dependencyTree, List<PhasedUnits> phasedUnitsOfDependencies, boolean forCompiledModule) {
        String moduleName = module.getNameAsString();
        boolean moduleLoadedFromSource = isModuleLoadedFromSource(moduleName);
        boolean isLanguageModule = module == module.getLanguageModule();
        
        // if this is for a module we're compiling, or for an indirectly imported module, we need to check because the
        // module in question will be in the classpath
        if(moduleLoadedFromSource || forCompiledModule){
            String standardisedModuleName = ModuleUtil.toCeylonModuleName(moduleName);
            // check for an already loaded module with the same name but different version
            for(Module loadedModule : getContext().getModules().getListOfModules()){
                String loadedModuleName = loadedModule.getNameAsString();
                String standardisedLoadedModuleName = ModuleUtil.toCeylonModuleName(loadedModuleName);
                boolean sameModule = loadedModuleName.equals(moduleName);
                boolean similarModule = standardisedLoadedModuleName.equals(standardisedModuleName);
                if((sameModule || similarModule)
                        && !loadedModule.getVersion().equals(module.getVersion())
                        && getModelLoader().isModuleInClassPath(loadedModule)){
                    // abort
                    // we need this error thrown rather than the typechecker error because the typechecker currently
                    // allows more than we do, such as having direct imports of the same module with different versions
                    // as long as they are not reexported, but we don't support that since they all go in the same
                    // classpath (direct imports of compiled modules)

                    if(sameModule){
                        String[] versions = VersionComparator.orderVersions(module.getVersion(), loadedModule.getVersion());
                        String error = "source code imports two different versions of module '" + 
                                moduleName + "': "+
                                "version '" + versions[0] + "' and version '" + versions[1] +
                                "'";
                        addErrorToModule(dependencyTree.getFirst(), error);
                    }else{
                        String moduleA;
                        String moduleB;
                        if(loadedModuleName.compareTo(moduleName) < 0){
                            moduleA = ModuleUtil.makeModuleName(loadedModuleName, loadedModule.getVersion());
                            moduleB = ModuleUtil.makeModuleName(moduleName, module.getVersion());
                        }else{
                            moduleA = ModuleUtil.makeModuleName(moduleName, module.getVersion());
                            moduleB = ModuleUtil.makeModuleName(loadedModuleName, loadedModule.getVersion());
                        }
                        String error = "source code imports two different versions of similar modules '" + 
                                moduleA + "' and '"+ moduleB + "'";
                        addWarningToModule(dependencyTree.getFirst(), Warning.similarModule, error);
                    }
                    return;
                }
            }
        }
        
        if(moduleLoadedFromSource){
            super.resolveModule(artifact, module, moduleImport, dependencyTree, phasedUnitsOfDependencies, forCompiledModule);
        }else if(forCompiledModule || isLanguageModule || shouldLoadTransitiveDependencies()){
            // we only add stuff to the classpath and load the modules if we need them to compile our modules
            getModelLoader().addModuleToClassPath(module, artifact); // To be able to load it from the corresponding archive
            if(!module.isDefault() && !getModelLoader().loadCompiledModule(module)){
                // we didn't find module.class so it must be a java module if it's not the default module
                ((LazyModule)module).setJava(true);
                module.setNative(Backend.Java.nativeAnnotation);
                
                List<ArtifactResult> deps = artifact.dependencies();
                for (ArtifactResult dep : deps) {
                    Module dependency = getOrCreateModule(ModuleManager.splitModuleName(dep.name()), dep.version());

                    ModuleImport depImport = findImport(module, dependency);
                    if (depImport == null) {
                        moduleImport = new ModuleImport(dependency, dep.importType() == ImportType.OPTIONAL, dep.importType() == ImportType.EXPORT, Backend.Java);
                        module.addImport(moduleImport);
                    }
                }
            }
            LazyModule lazyModule = (LazyModule) module;
            if(!lazyModule.isJava() && !module.isDefault()){
                // it must be a Ceylon module
                // default modules don't have any module descriptors so we can't check them
                Overrides overrides = getContext().getRepositoryManager().getOverrides();
                if (overrides != null) {
                    if (overrides.getArtifactOverrides(new ArtifactContext(artifact.name(), artifact.version())) != null) {
                        Set<ModuleDependencyInfo> existingModuleDependencies = new HashSet<>();
                        for (ModuleImport i : lazyModule.getImports()) {
                            Module m = i.getModule();
                            if (m != null) {
                                existingModuleDependencies.add(new ModuleDependencyInfo(m.getNameAsString(), m.getVersion(), i.isOptional(), i.isExport()));
                            }
                        }
                        ModuleInfo sourceModuleInfo = new ModuleInfo(null, existingModuleDependencies);
                        ModuleInfo newModuleInfo = overrides.applyOverrides(artifact.name(), artifact.version(), sourceModuleInfo);
                        List<ModuleImport> newModuleImports = new ArrayList<>();
                        for (ModuleDependencyInfo dep : newModuleInfo.getDependencies()) {
                            Module dependency = getOrCreateModule(ModuleManager.splitModuleName(dep.getName()), dep.getVersion());
                            Backend backend = null; // TODO Figure out if this dependency is only for a specific backend
                            moduleImport = new ModuleImport(dependency, dep.isOptional(), dep.isExport(), backend);
                            newModuleImports.add(moduleImport);
                        }
                        module.overrideImports(newModuleImports);
                    }
                }
                if(!Versions.isJvmBinaryVersionSupported(lazyModule.getMajor(), lazyModule.getMinor())){
                    attachErrorToDependencyDeclaration(moduleImport,
                            dependencyTree,
                            "This module was compiled for an incompatible version of the Ceylon compiler ("+lazyModule.getMajor()+"."+lazyModule.getMinor()+")."
                                    +"\nThis compiler supports "+Versions.JVM_BINARY_MAJOR_VERSION+"."+Versions.JVM_BINARY_MINOR_VERSION+"."
                                    +"\nPlease try to recompile your module using a compatible compiler."
                                    +"\nBinary compatibility will only be supported after Ceylon 1.2.");
                }
            }
            // module is now available
            module.setAvailable(true);
        }
    }

    /**
     * To be overriden by reflection module manager, because reflection requires even types of private members
     * of imported modules to be in the classpath, and those could be of unimported modules (since they're private
     * that's allowed).
     */
    protected boolean shouldLoadTransitiveDependencies(){
        return false;
    }
    
    @Override
    protected abstract Module createModule(List<String> moduleName, String version);
    
    protected abstract AbstractModelLoader getModelLoader();

    /**
     * Return true if this module should be loaded from source we are compiling
     * and not from its compiled artifact at all. Returns false by default, so
     * modules will be laoded from their compiled artifact.
     */
    protected boolean isModuleLoadedFromSource(String moduleName){
        return false;
    }
    
    @Override
    public Iterable<String> getSearchedArtifactExtensions() {
        return Arrays.asList("car", "jar");
    }

    @Override
    public boolean supportsBackend(Backend backend) {
        return backend == Backend.Java;
    }
    
    @Override
    public void addImplicitImports() {
        Module languageModule = getContext().getModules().getLanguageModule();
        for(Module m : getContext().getModules().getListOfModules()){
            // Java modules don't depend on ceylon.language
            if((m instanceof LazyModule == false || !((LazyModule)m).isJava()) && !m.equals(languageModule)) {
                // add ceylon.language if required
                ModuleImport moduleImport = findImport(m, languageModule);
                if (moduleImport == null) {
                    moduleImport = new ModuleImport(languageModule, false, true);
                    m.addImport(moduleImport);
                }
            }
        }
    }
    
    @Override
    public void attachErrorToDependencyDeclaration(ModuleImport moduleImport, List<Module> dependencyTree, String error) {
        // special case for the java modules, which we only get when using the wrong version
        String name = moduleImport.getModule().getNameAsString();
        if(AbstractModelLoader.isJDKModule(name)){
            error = "imported module '" + name + "' depends on JDK version '\"" + 
                    moduleImport.getModule().getVersion() +
                    "\"' and you're compiling with Java " + JDKUtils.jdk.version;
        }
        super.attachErrorToDependencyDeclaration(moduleImport, dependencyTree, error);
    }

    @Override
    protected boolean compareVersions(Module current, String version, String currentVersion) {
        String name = current.getNameAsString();
        if(JDKUtils.isJDKModule(name) || JDKUtils.isOracleJDKModule(name)){
            // if we're running JDK8, pretend that it provides JDK7 modules
            if(JDKUtils.jdk.providesVersion(version)
                    && JDKUtils.jdk.providesVersion(currentVersion))
                return true;
        }
        return currentVersion == null || version == null || currentVersion.equals(version);
    }
    
    @Override
    public void addModuleDependencyDefinition(ModuleImport moduleImport, Node definition) {
        super.addModuleDependencyDefinition(moduleImport, definition);
        Module module = moduleImport.getModule();
        if(module == null)
            return;
        String nameAsString = module.getNameAsString();
        String version = module.getVersion();
        if(version != null
                && AbstractModelLoader.isJDKModule(nameAsString)){
            // Add a warning if we're using a lower JDK than the one we're running on
            if(JDKUtils.jdk.isLowerVersion(version)){
                definition.addUsageWarning(Warning.importsOtherJdk, "You import JDK7, which is provided by the JDK8 you are running on, but"+
                        " we cannot check that you are not using any JDK8-specific classes or methods. Upgrade your import to JDK8 if you depend on"+
                        " JDK8 classes or methods.");
            }
        }
    }
}
