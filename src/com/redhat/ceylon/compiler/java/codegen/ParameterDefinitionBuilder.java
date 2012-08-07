package com.redhat.ceylon.compiler.java.codegen;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

public class ParameterDefinitionBuilder {

    private final AbstractTransformer gen;
    
    private long modifiers;
    
    private JCExpression type;
    
    private List<JCAnnotation> typeAnnos;

    private boolean sequenced;

    private boolean defaulted;
    
    private final String name;
    
    private String aliasedName;
    
    private boolean noAnnotations = false;
    
    private boolean built = false;

    private ParameterDefinitionBuilder(AbstractTransformer gen, String name) {
        this.gen = gen;
        this.name = name;
    }
    
    static ParameterDefinitionBuilder instance(AbstractTransformer gen, String name) {
        return new ParameterDefinitionBuilder(gen, name);
    }

    public ParameterDefinitionBuilder modifiers(long mods) {
        this.modifiers |= mods;
        return this;
    }
    
    public ParameterDefinitionBuilder type(JCExpression type, List<JCAnnotation> typeAnnos) {
        this.type = type;
        this.typeAnnos = typeAnnos;
        return this;
    }
    
    public ParameterDefinitionBuilder sequenced(boolean sequenced) {
        this.sequenced = sequenced;
        return this;
    }
    
    public ParameterDefinitionBuilder aliasName(String aliasedName) {
        this.aliasedName = aliasedName;
        return this;
    }
    
    public ParameterDefinitionBuilder defaulted(boolean defaulted) {
        this.defaulted = defaulted;
        return this;
    }
    
    public ParameterDefinitionBuilder noAnnotations() {
        noAnnotations = true;
        return this;
    }
    
    public JCVariableDecl build() {
        if (built) {
            throw new IllegalStateException();
        }
        built = true;
        List<JCAnnotation> annots = List.nil();
        if (!noAnnotations) {
            annots = annots.appendList(gen.makeAtName(name));
            if (sequenced) {
                annots = annots.appendList(gen.makeAtSequenced());
            }
            if (defaulted) {
                annots = annots.appendList(gen.makeAtDefaulted());
            }
            if (typeAnnos != null) {
                annots = annots.appendList(typeAnnos);
            }
        }
        Name name = gen.names().fromString(aliasedName != null ? aliasedName : this.name);
        return gen.make().VarDef(gen.make().Modifiers(modifiers | Flags.PARAMETER, annots), 
                name, type, null);   
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Flags.toString(modifiers)).append(' ');
        sb.append(type).append(' ');
        sb.append(aliasedName != null ? aliasedName : name);
        return sb.toString();
    }
    
}
