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
package com.redhat.ceylon.compiler.java.test.structure;

import org.junit.Ignore;
import org.junit.Test;

import com.redhat.ceylon.compiler.java.test.CompilerTest;

public class NestingTest extends CompilerTest {
    /*    
    @Test
    public void testNstNesting(){
        compareWithJavaSource("nesting/Nesting");
    }*/
    @Test
    public void testNstCcc(){
        compareWithJavaSource("nesting/ccc/CCC");
    }
    @Test
    public void testNstCci(){
        compareWithJavaSource("nesting/cci/CCI");
    }
    @Test
    public void testNstCic(){
        compareWithJavaSource("nesting/cic/CIC");
    }
    @Test
    public void testNstCii(){
        compareWithJavaSource("nesting/cii/CII");
    }
    @Test
    public void testNstIcc(){
        compareWithJavaSource("nesting/icc/ICC");
    }
    @Test
    public void testNstIci(){
        compareWithJavaSource("nesting/ici/ICI");
    }
    @Test
    public void testNstIic(){
        compareWithJavaSource("nesting/iic/IIC");
    }
    @Test
    public void testNstIii(){
        compareWithJavaSource("nesting/iii/III");
    }
    @Test
    public void testNstLocals(){
        compareWithJavaSource("nesting/Locals");
    }
    
    @Test
    public void testNstClassMethodDefaultedParameter(){
        compareWithJavaSource("nesting/ClassMethodDefaultedParameter");
    }
    
    @Test
    public void testNstFunctionDefaultedParameter(){
        compareWithJavaSource("nesting/FunctionDefaultedParameter");
    }
    
    @Test
    public void testNstClassInitDefaultedParameter(){
        compareWithJavaSource("nesting/ClassInitDefaultedParameter");
    }
    
    @Test
    public void testNstInterfaceMethodDefaultedParameter(){
        compareWithJavaSource("nesting/InterfaceMethodDefaultedParameter");
    }

    @Test
    public void testNstConcreteInterface(){
        compareWithJavaSource("nesting/ConcreteInterface");
    }
    
    @Test
    public void testNstNestedInterface(){
        compareWithJavaSource("nesting/NestedInterface");
    }
    
}
