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
@nomodel
class MethodForIterator(){
    shared void m(Sequence<String> seq){
        for(String s in seq){
            // Empty
        }
    }
    shared void m2(){
        for(String s in {"aap","noot","mies"}){
            // Empty
        }
        for(String? s in {"aap",null,"mies"}){
            // Empty
        }
        for(Natural n in {1,2,3}){
            // Empty
        }
        for(Natural? n in {1,null,3}){
            // Empty
        }
    }
}