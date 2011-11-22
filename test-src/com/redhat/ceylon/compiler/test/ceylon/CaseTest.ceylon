/*
 * Copyright Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the authors tag. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * 
 * This particular file is subject to the "Classpath" exception as provided in the 
 * LICENSE file that accompanied this code.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
shared class CaseTest() extends Test() {

	class Color (String name) extends Case(name) {}

	@test
	shared void testString() {
		Color red = Color("red");	
		assertEquals("red", red.string);		
	}
		
	@test
	shared void testEquals() {
		Color red = Color("red");
		Color anotherRed = Color("red");
		Color yellow =  Color("yellow");		
		assertFalse(yellow == red);
		assertTrue(yellow == yellow);		
		assertFalse(red == anotherRed);		
	}
	
	@test
	shared void testHash() {
		Color red = Color("red");
		Color green = Color("green");	
		Integer redHash = red.hash;	
		Integer greenHash = green.hash;
		assertTrue(redHash != greenHash);	
	}	
	
}	  