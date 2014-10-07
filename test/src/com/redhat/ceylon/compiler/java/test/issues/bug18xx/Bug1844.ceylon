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
@nomodel
void bug1844v(String expression, Boolean isAnythingPlus, {Anything*} eval) {
    assert(eval is {String*});
    if(isAnythingPlus){
        assert(eval is {Anything+});
    }else{
        assert(!eval is {Anything+});
    }
}

@nomodel
shared void bug1844() {
    bug1844v("{}", false, {});                 // v({ })
    bug1844v { "named <empty>"; false; };      // v { }
    bug1844v("{\"a\"}", true, {"a"});         // v({ "a" })
    value x = {"a"};
    bug1844v("let x={\"a\"} in x", true, x);  // v(x)
    bug1844v { "named \"a\""; true; "a" };    // v { "a" }
}
