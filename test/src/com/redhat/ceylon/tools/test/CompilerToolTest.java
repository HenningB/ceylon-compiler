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
package com.redhat.ceylon.tools.test;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.ceylon.common.tool.OptionArgumentException;
import com.redhat.ceylon.common.tool.ToolFactory;
import com.redhat.ceylon.common.tool.ToolLoader;
import com.redhat.ceylon.common.tool.ToolModel;
import com.redhat.ceylon.compiler.CeylonCompileTool;
import com.redhat.ceylon.compiler.CompilerBugException;
import com.redhat.ceylon.compiler.CompilerErrorException;
import com.redhat.ceylon.compiler.SystemErrorException;
import com.redhat.ceylon.tools.CeylonToolLoader;

public class CompilerToolTest {
    
    protected final ToolFactory pluginFactory = new ToolFactory();
    protected final ToolLoader pluginLoader = new CeylonToolLoader(null);

    @Test
    public void testNoModules()  throws Exception {
        ToolModel<CeylonCompileTool> model = pluginLoader.loadToolModel("compile");
        Assert.assertNotNull(model);
        try {
            pluginFactory.bindArguments(model, Collections.<String>emptyList());
            Assert.fail();
        } catch (OptionArgumentException e) {
            Assert.assertEquals("Argument moduleOrFile should appear at least 1 time(s)", e.getMessage());
        }
    }
    
    @Test
    public void testCompile()  throws Exception {
        ToolModel<CeylonCompileTool> model = pluginLoader.loadToolModel("compile");
        Assert.assertNotNull(model);
        CeylonCompileTool tool = pluginFactory.bindArguments(model, 
                Arrays.asList("--src=test/src", "com.redhat.ceylon.tools.test.ceylon"));
        tool.run();
    }
    
    @Test
    public void testCompileVerbose()  throws Exception {
        ToolModel<CeylonCompileTool> model = pluginLoader.loadToolModel("compile");
        Assert.assertNotNull(model);
        CeylonCompileTool tool = pluginFactory.bindArguments(model, 
                Arrays.asList("--verbose", "--src=test/src", "com.redhat.ceylon.tools.test.ceylon"));
        tool.run();
    }
    
    @Test
    public void testCompileWithSyntaxErrors()  throws Exception {
        ToolModel<CeylonCompileTool> model = pluginLoader.loadToolModel("compile");
        Assert.assertNotNull(model);
        CeylonCompileTool tool = pluginFactory.bindArguments(model, 
                Arrays.asList("--src=test/src", "com.redhat.ceylon.tools.test.syntax"));
        try{
            tool.run();
            Assert.fail("Tool should have thrown an exception");
        }catch(CompilerErrorException x){
            Assert.assertEquals("There was 1 error", x.getMessage());
        }catch(Throwable t){
            t.printStackTrace();
            Assert.fail("Unexpected exception");
        }
    }
    
    @Test
    public void testCompileWithAnalysisErrors()  throws Exception {
        ToolModel<CeylonCompileTool> model = pluginLoader.loadToolModel("compile");
        Assert.assertNotNull(model);
        CeylonCompileTool tool = pluginFactory.bindArguments(model, 
                Arrays.asList("--src=test/src", "com.redhat.ceylon.tools.test.analysis"));
        try{
            tool.run();
            Assert.fail("Tool should have thrown an exception");
        }catch(CompilerErrorException x){
            Assert.assertEquals("There were 3 errors", x.getMessage());
        }catch(Throwable t){
            t.printStackTrace();
            Assert.fail("Unexpected exception");
        }
    }
  
    @Test
    public void testCompileWithErroneous()  throws Exception {
        ToolModel<CeylonCompileTool> model = pluginLoader.loadToolModel("compile");
        Assert.assertNotNull(model);
        CeylonCompileTool tool = pluginFactory.bindArguments(model, 
                Arrays.asList("--src=test/src", "com.redhat.ceylon.tools.test.erroneous"));
        try{
            tool.run();
            Assert.fail("Tool should have thrown an exception");
        }catch(CompilerBugException x){
            Assert.assertEquals("Codegen Assertion", x.getMessage());
        }catch(Throwable t){
            t.printStackTrace();
            Assert.fail("Unexpected exception");
        }
    }
    
    @Test
    public void testCompileWithRuntimeException()  throws Exception {
        ToolModel<CeylonCompileTool> model = pluginLoader.loadToolModel("compile");
        Assert.assertNotNull(model);
        CeylonCompileTool tool = pluginFactory.bindArguments(model, 
                Arrays.asList("--src=test/src", "com.redhat.ceylon.tools.test.runtimeex"));
        try{
            tool.run();
            Assert.fail("Tool should have thrown an exception");
        }catch(CompilerBugException x){
            Assert.assertEquals("Codegen Bug", x.getMessage());
        }catch(Throwable t){
            t.printStackTrace();
            Assert.fail("Unexpected exception");
        }
    }
    
    @Test
    public void testCompileWithOomeException()  throws Exception {
        ToolModel<CeylonCompileTool> model = pluginLoader.loadToolModel("compile");
        Assert.assertNotNull(model);
        CeylonCompileTool tool = pluginFactory.bindArguments(model, 
                Arrays.asList("--src=test/src", "com.redhat.ceylon.tools.test.oome"));
        try{
            tool.run();
            Assert.fail("Tool should have thrown an exception");
        }catch(SystemErrorException x){
            Assert.assertEquals("java.lang.OutOfMemoryError", x.getMessage());
        }catch(Throwable t){
            t.printStackTrace();
            Assert.fail("Unexpected exception");
        }
    }
    
    @Test
    public void testCompileWithStackOverflowError()  throws Exception {
        ToolModel<CeylonCompileTool> model = pluginLoader.loadToolModel("compile");
        Assert.assertNotNull(model);
        CeylonCompileTool tool = pluginFactory.bindArguments(model, 
                Arrays.asList("--src=test/src", "com.redhat.ceylon.tools.test.stackoverflow"));
        try{
            tool.run();
            Assert.fail("Tool should have thrown an exception");
        }catch(SystemErrorException x){
            Assert.assertEquals("java.lang.StackOverflowError", x.getMessage());
        }catch(Throwable t){
            t.printStackTrace();
            Assert.fail("Unexpected exception");
        }
    }
    
    @Test
    public void testBug1179()  throws Exception {
        ToolModel<CeylonCompileTool> model = pluginLoader.loadToolModel("compile");
        Assert.assertNotNull(model);
        try{
            CeylonCompileTool tool = pluginFactory.bindArguments(model, 
                    Arrays.asList("--src=test/src", "3"));
            Assert.fail("Tool should have thrown an exception");
        }catch(OptionArgumentException x){
            Assert.assertEquals("Not a valid module name or source file: 3", x.getMessage());
        }catch(Throwable t){
            t.printStackTrace();
            Assert.fail("Unexpected exception");
        }
    }
}
