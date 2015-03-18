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
package com.redhat.ceylon.compiler.java.test.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.Assert;

import com.redhat.ceylon.compiler.loader.ModelLoader;
import com.redhat.ceylon.compiler.loader.ModelResolutionException;
import com.redhat.ceylon.compiler.loader.TypeParser;
import com.redhat.ceylon.compiler.loader.TypeParserException;
import com.redhat.ceylon.compiler.typechecker.model.Declaration;
import com.redhat.ceylon.compiler.typechecker.model.IntersectionType;
import com.redhat.ceylon.compiler.typechecker.model.Module;
import com.redhat.ceylon.compiler.typechecker.model.ProducedType;
import com.redhat.ceylon.compiler.typechecker.model.Class;
import com.redhat.ceylon.compiler.typechecker.model.Package;
import com.redhat.ceylon.compiler.typechecker.model.Scope;
import com.redhat.ceylon.compiler.typechecker.model.SiteVariance;
import com.redhat.ceylon.compiler.typechecker.model.TypeDeclaration;
import com.redhat.ceylon.compiler.typechecker.model.TypeParameter;
import com.redhat.ceylon.compiler.typechecker.model.UnionType;
import com.redhat.ceylon.compiler.typechecker.model.Unit;

public class TypeParserTests {
    
    private static final Unit mockUnit = new Unit();
    private static final Module mockModule = new Module(){
        private Package mockPackage = new Package(){
            public Module getModule() {
                mockModule.setName(Arrays.asList(""));
                return mockModule;
            }
        };
        public Package getPackage(String name) {
            mockPackage.setName(Arrays.asList(""));
            return mockPackage;
        }
        
    };
    
    static class MockLoader implements ModelLoader {

        static final ModelLoader instance = new MockLoader();

        private Map<String, Class> classes = new HashMap<String,Class>();
        private Module mod = mockModule;
        private Package pkg = mockModule.getPackage("");
        private Unit unit = mockUnit;
        
        private MockLoader(){
            mod.setName(Arrays.asList(""));
            mod.setLanguageModule(new Module());
            pkg.setName(Arrays.asList(""));
            pkg.setModule(mod);
            unit.setPackage(pkg);
            Class a = makeClass("a");
            makeClass("b", a);
            makeClass("b");
            makeClass("c");
            makeClass("d");
            makeClass("e");
            makeClass("f");
            Class t2 = makeParameterisedClass("t2");
            makeParameterisedClass("t2", t2);
            Package otherPkg = new Package();
            otherPkg.setName(Arrays.asList("pkg"));
            makeClass("a", otherPkg);
            Class b = makeClass("b", otherPkg);
            makeClass("c", b);
        }

        
        @Override
        public ProducedType getType(Module module, String pkg, String name, Scope scope) {
            Class klass = (Class)getDeclaration(module, pkg, name, scope);
            return klass.getType();
        }

        @Override
        public Declaration getDeclaration(Module module, String pkg, String name, Scope scope) {
            Class klass = classes.get(name);
            if(klass == null)
                throw new ModelResolutionException("Unknown type: "+name);
            return klass;
        }
        
        private Class makeParameterisedClass(String name) {
            return makeParameterisedClass(name, null);
        }
        
        private Class makeParameterisedClass(String name, Class container) {
            Class klass = makeClass(name, container);
            List<TypeParameter> typeParameters = new ArrayList<TypeParameter>(2);
            TypeParameter typeParam = new TypeParameter();
            typeParam.setName("A");
            typeParameters.add(typeParam);
            typeParam = new TypeParameter();
            typeParam.setName("B");
            typeParameters.add(typeParam);
            klass.setTypeParameters(typeParameters );
            return klass;
        }

        private Class makeClass(String name) {
            return makeClass(name, null);
        }
        
        private Class makeClass(String name, Scope container) {
            Class klass = new Class();
            klass.setName(name);
            klass.setUnit(unit);
            klass.setShared(true);
            if(container != null){
                container.addMember(klass);
                klass.setContainer(container);
                classes.put(container.getQualifiedNameString()+"."+name, klass);
            }else{
                klass.setContainer(pkg);
                classes .put(name, klass);
            }
            return klass;
        }

        @Override
        public Declaration getDeclaration(Module module, String typeName, DeclarationType declarationType) {
            throw new RuntimeException("Not yet implemented");
        }


        @Override
        public Module getLoadedModule(String moduleName, String version) {
            throw new RuntimeException("Not yet implemented");
        }
        
    }
    
    @Test
    public void testUnion(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("a|b|c", null, mockModule, mockUnit);
        Assert.assertNotNull(type);
        TypeDeclaration declaration = type.getDeclaration();
        Assert.assertNotNull(declaration);
        Assert.assertTrue(declaration instanceof UnionType);
        UnionType union = (UnionType) declaration;
        List<ProducedType> types = union.getCaseTypes();
        Assert.assertEquals(3, types.size());
        Assert.assertEquals("a", types.get(0).getDeclaration().getName());
        Assert.assertTrue(types.get(0).getDeclaration() instanceof Class);
        Assert.assertEquals("b", types.get(1).getDeclaration().getName());
        Assert.assertTrue(types.get(1).getDeclaration() instanceof Class);
        Assert.assertEquals("c", types.get(2).getDeclaration().getName());
        Assert.assertTrue(types.get(2).getDeclaration() instanceof Class);
    }

    @Test
    public void testIntersection(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("a&b&c", null, mockModule, mockUnit);
        Assert.assertNotNull(type);
        TypeDeclaration declaration = type.getDeclaration();
        Assert.assertNotNull(declaration);
        Assert.assertTrue(declaration instanceof IntersectionType);
        IntersectionType intersection = (IntersectionType) declaration;
        List<ProducedType> types = intersection.getSatisfiedTypes();
        Assert.assertEquals(3, types.size());
        Assert.assertEquals("a", types.get(0).getDeclaration().getName());
        Assert.assertTrue(types.get(0).getDeclaration() instanceof Class);
        Assert.assertEquals("b", types.get(1).getDeclaration().getName());
        Assert.assertTrue(types.get(1).getDeclaration() instanceof Class);
        Assert.assertEquals("c", types.get(2).getDeclaration().getName());
        Assert.assertTrue(types.get(2).getDeclaration() instanceof Class);
    }

    @Test
    public void testIntersectionAndUnion(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("a&b|c", null, mockModule, mockUnit);
        Assert.assertNotNull(type);
        TypeDeclaration declaration = type.getDeclaration();
        Assert.assertNotNull(declaration);
        Assert.assertTrue(declaration instanceof UnionType);
        UnionType union = (UnionType) declaration;
        List<ProducedType> unionTypes = union.getCaseTypes();
        Assert.assertEquals(2, unionTypes.size());
        
        Assert.assertTrue(unionTypes.get(0).getDeclaration() instanceof IntersectionType);
        IntersectionType intersection = (IntersectionType) unionTypes.get(0).getDeclaration();

        List<ProducedType> intersectionTypes = intersection.getSatisfiedTypes();
        Assert.assertEquals(2, intersectionTypes.size());
        Assert.assertEquals("a", intersectionTypes.get(0).getDeclaration().getName());
        Assert.assertTrue(intersectionTypes.get(0).getDeclaration() instanceof Class);
        Assert.assertEquals("b", intersectionTypes.get(1).getDeclaration().getName());
        Assert.assertTrue(intersectionTypes.get(1).getDeclaration() instanceof Class);

        Assert.assertEquals("c", unionTypes.get(1).getDeclaration().getName());
        Assert.assertTrue(unionTypes.get(1).getDeclaration() instanceof Class);
    }

    @Test
    public void testParams(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("t2<b,c>", null, mockModule, mockUnit);
        assertTypeWithParameters(type);
        
        Assert.assertTrue(type.getVarianceOverrides().isEmpty());
    }

    @Test
    public void testParamsVariance1(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("t2<in b,out c>", null, mockModule, mockUnit);
        assertTypeWithParameters(type);

        Map<TypeParameter, SiteVariance> varianceOverrides = type.getVarianceOverrides();
        Assert.assertNotNull(varianceOverrides);
        Assert.assertEquals(2, varianceOverrides.size());
        List<TypeParameter> tps = type.getDeclaration().getTypeParameters();
        Assert.assertEquals(SiteVariance.IN, varianceOverrides.get(tps.get(0)));
        Assert.assertEquals(SiteVariance.OUT, varianceOverrides.get(tps.get(1)));
    }

    @Test
    public void testParamsVariance2(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("t2<b,out c>", null, mockModule, mockUnit);
        assertTypeWithParameters(type);

        Map<TypeParameter, SiteVariance> varianceOverrides = type.getVarianceOverrides();
        Assert.assertNotNull(varianceOverrides);
        Assert.assertEquals(1, varianceOverrides.size());
        List<TypeParameter> tps = type.getDeclaration().getTypeParameters();
        Assert.assertEquals(null, varianceOverrides.get(tps.get(0)));
        Assert.assertEquals(SiteVariance.OUT, varianceOverrides.get(tps.get(1)));
    }

    @Test
    public void testParamsVariance3(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("t2<in b,c>", null, mockModule, mockUnit);
        assertTypeWithParameters(type);

        Map<TypeParameter, SiteVariance> varianceOverrides = type.getVarianceOverrides();
        Assert.assertNotNull(varianceOverrides);
        Assert.assertEquals(1, varianceOverrides.size());
        List<TypeParameter> tps = type.getDeclaration().getTypeParameters();
        Assert.assertEquals(SiteVariance.IN, varianceOverrides.get(tps.get(0)));
        Assert.assertEquals(null, varianceOverrides.get(tps.get(1)));
    }

    private void assertTypeWithParameters(ProducedType type) {
        Assert.assertNotNull(type);
        TypeDeclaration declaration = type.getDeclaration();
        Assert.assertNotNull(declaration);
        Assert.assertTrue(declaration instanceof Class);
        Assert.assertEquals("t2", declaration.getName());
        List<ProducedType> tal = type.getTypeArgumentList();
        Assert.assertEquals(2, tal.size());
        Assert.assertEquals("b", tal.get(0).getDeclaration().getName());
        Assert.assertTrue(tal.get(0).getDeclaration() instanceof Class);
        Assert.assertEquals("c", tal.get(1).getDeclaration().getName());
        Assert.assertTrue(tal.get(1).getDeclaration() instanceof Class);
    }

    @Test
    public void testUnionParams(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("a|t2<b|c,t2<d,e|f>>", null, mockModule, mockUnit);
        Assert.assertNotNull(type);
        TypeDeclaration declaration = type.getDeclaration();
        Assert.assertNotNull(declaration);
        Assert.assertTrue(declaration instanceof UnionType);
        UnionType union = (UnionType) declaration;
        List<ProducedType> caseTypes = union.getCaseTypes();
        Assert.assertEquals(2, caseTypes.size());
        
        // a
        Assert.assertEquals("a", caseTypes.get(0).getDeclaration().getName());
        Assert.assertTrue(caseTypes.get(0).getDeclaration() instanceof Class);
        
        // first t2
        ProducedType firstT2 = caseTypes.get(1);
        TypeDeclaration firstT2Declaration = firstT2.getDeclaration();
        Assert.assertNotNull(firstT2Declaration);
        Assert.assertTrue(firstT2Declaration instanceof Class);
        Assert.assertEquals("t2", firstT2Declaration.getName());
        Assert.assertEquals(2, firstT2.getTypeArgumentList().size());
        
        // b|c
        ProducedType bc = firstT2.getTypeArgumentList().get(0);
        Assert.assertTrue(bc.getDeclaration() instanceof UnionType);
        Assert.assertEquals(2, bc.getDeclaration().getCaseTypes().size());
        
        // b
        ProducedType b = bc.getDeclaration().getCaseTypes().get(0);
        Assert.assertEquals("b", b.getDeclaration().getName());
        Assert.assertTrue(b.getDeclaration() instanceof Class);

        // c
        ProducedType c = bc.getDeclaration().getCaseTypes().get(1);
        Assert.assertEquals("c", c.getDeclaration().getName());
        Assert.assertTrue(c.getDeclaration() instanceof Class);
        
        // second t2
        ProducedType secondT2 = firstT2.getTypeArgumentList().get(1);
        TypeDeclaration secondT2Declaration = firstT2.getDeclaration();
        Assert.assertNotNull(secondT2Declaration);
        Assert.assertTrue(secondT2Declaration instanceof Class);
        Assert.assertEquals("t2", secondT2Declaration.getName());
        Assert.assertEquals(2, secondT2.getTypeArgumentList().size());
        
        // d
        ProducedType d = secondT2.getTypeArgumentList().get(0);
        Assert.assertEquals("d", d.getDeclaration().getName());
        Assert.assertTrue(d.getDeclaration() instanceof Class);

        // e|f
        ProducedType ef = secondT2.getTypeArgumentList().get(1);
        Assert.assertTrue(ef.getDeclaration() instanceof UnionType);
        Assert.assertEquals(2, ef.getDeclaration().getCaseTypes().size());
        
        // e
        ProducedType e = ef.getDeclaration().getCaseTypes().get(0);
        Assert.assertEquals("e", e.getDeclaration().getName());
        Assert.assertTrue(e.getDeclaration() instanceof Class);

        // f
        ProducedType f = ef.getDeclaration().getCaseTypes().get(1);
        Assert.assertEquals("f", f.getDeclaration().getName());
        Assert.assertTrue(f.getDeclaration() instanceof Class);

    }

    @Test
    public void testQualified(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("a.b", null, mockModule, mockUnit);
        Assert.assertNotNull(type);
        TypeDeclaration declaration = type.getDeclaration();
        Assert.assertNotNull(declaration);
        Assert.assertTrue(declaration instanceof Class);
        Assert.assertEquals("a.b", declaration.getQualifiedNameString());

        ProducedType qualifyingType = type.getQualifyingType();
        Assert.assertNotNull(qualifyingType);
        TypeDeclaration qualifyingDeclaration = qualifyingType.getDeclaration();
        Assert.assertNotNull(qualifyingDeclaration);
        Assert.assertTrue(qualifyingDeclaration instanceof Class);
        Assert.assertEquals("a", qualifyingDeclaration.getName());
    }

    @Test
    public void testQualifiedAndParameterised(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("t2<a,b>.t2<c,d>", null, mockModule, mockUnit);
        Assert.assertNotNull(type);
        TypeDeclaration declaration = type.getDeclaration();
        Assert.assertNotNull(declaration);
        Assert.assertTrue(declaration instanceof Class);
        Assert.assertEquals("t2.t2", declaration.getQualifiedNameString());
        Assert.assertEquals(2, type.getTypeArgumentList().size());
        
        // c
        ProducedType c = type.getTypeArgumentList().get(0);
        Assert.assertEquals("c", c.getDeclaration().getName());
        Assert.assertTrue(c.getDeclaration() instanceof Class);

        // d
        ProducedType d = type.getTypeArgumentList().get(1);
        Assert.assertEquals("d", d.getDeclaration().getName());
        Assert.assertTrue(d.getDeclaration() instanceof Class);

        ProducedType qualifyingType = type.getQualifyingType();
        Assert.assertNotNull(qualifyingType);
        TypeDeclaration qualifyingDeclaration = qualifyingType.getDeclaration();
        Assert.assertNotNull(qualifyingDeclaration);
        Assert.assertTrue(qualifyingDeclaration instanceof Class);
        Assert.assertEquals("t2", qualifyingDeclaration.getName());
        Assert.assertEquals(2, qualifyingType.getTypeArgumentList().size());
        
        // a
        ProducedType a = qualifyingType.getTypeArgumentList().get(0);
        Assert.assertEquals("a", a.getDeclaration().getName());
        Assert.assertTrue(a.getDeclaration() instanceof Class);

        // b
        ProducedType b = qualifyingType.getTypeArgumentList().get(1);
        Assert.assertEquals("b", b.getDeclaration().getName());
        Assert.assertTrue(b.getDeclaration() instanceof Class);
    }

    @Test
    public void testPackageQualified(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("pkg::b", null, mockModule, mockUnit);
        Assert.assertNotNull(type);
        TypeDeclaration declaration = type.getDeclaration();
        Assert.assertNotNull(declaration);
        Assert.assertTrue(declaration instanceof Class);
        Assert.assertEquals("pkg::b", declaration.getQualifiedNameString());

        Assert.assertNull(type.getQualifyingType());
    }

    @Test
    public void testComplexQualified(){
        ProducedType type = new TypeParser(MockLoader.instance).decodeType("<pkg::a&pkg::b>.c", null, mockModule, mockUnit);
        Assert.assertNotNull(type);
        TypeDeclaration declaration = type.getDeclaration();
        Assert.assertNotNull(declaration);
        Assert.assertTrue(declaration instanceof Class);
        Assert.assertEquals("pkg::b.c", declaration.getQualifiedNameString());

        ProducedType qualifyingType = type.getQualifyingType();
        Assert.assertNotNull(qualifyingType);
        TypeDeclaration qualifyingDeclaration = qualifyingType.getDeclaration();
        Assert.assertNotNull(qualifyingDeclaration);
        Assert.assertTrue(qualifyingDeclaration instanceof IntersectionType);
        Assert.assertEquals("a&b", qualifyingDeclaration.getName());
    }

    @Test(expected = ModelResolutionException.class)
    public void testParameterisedPackage(){
        new TypeParser(MockLoader.instance).decodeType("unknown<a>.b", null, mockModule, mockUnit);
    }

    @Test(expected = ModelResolutionException.class)
    public void testUnknownMember(){
        new TypeParser(MockLoader.instance).decodeType("a.unknown", null, mockModule, mockUnit);
    }

    @Test(expected = TypeParserException.class)
    public void testInvalidType(){
        new TypeParser(MockLoader.instance).decodeType("t2<a,b", null, mockModule, mockUnit);
    }
}
