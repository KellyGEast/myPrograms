import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a Carrot program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       RepeatStmtNode      ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// %%%ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
	boolean debug = false; // debug flag
	static public int offset; // offset tracker
	
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void addIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }
    
    // quick method to turn integer to string
    public String int2str(int i) {
    		return(Integer.toString(i));
    }
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /**
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, struct defintions, and functions in the program.
     */
    public void nameAnalysis() {
    		
    		SymTable symTab = new SymTable();
    		// true param indicates a global declaration
        myDeclList.nameAnalysis(symTab, true); 
        
        // check for main function
        if (symTab.lookupGlobal("main") == null) {
        		ErrMsg.fatal(0, 0, "No main function");
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck() {
        myDeclList.typeCheck();
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen(PrintWriter p) {
    		Codegen.p = p;
    		Codegen.createStringMap(); // create map for StringLit node storage
    		myDeclList.codeGen(true); // call code gen with global flag set
    		if (debug) {System.out.println("codeGen for programNode complete");};
    }
    
    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {

	public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

	/**
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        nameAnalysis(symTab, symTab, false);
    }
    
    public void nameAnalysis(SymTable symTab, boolean globalFlag) {
        nameAnalysis(symTab, symTab, globalFlag);
    }
    
    public void nameAnalysis(SymTable symTab, SymTable globalTab) {
        nameAnalysis(symTab, globalTab, false);
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab and a global symbol table globalTab
     * (for processing struct names in variable decls), process all of the 
     * decls in the list.
     */    
    public void nameAnalysis(SymTable symTab, SymTable globalTab, boolean globalFlag) {
    		//code gen update - add code to handle offset differences for local and global
    			
    			for (DeclNode node : myDecls) {
    	            if (node instanceof VarDeclNode) {
    	                Sym thisSym = ((VarDeclNode)node).nameAnalysis(symTab, globalTab);
    	                if (!globalFlag) {
    	                		// local
    	                		if (debug) {System.out.println("local var: "
    	                				+thisSym.toString()+" offset: "+offset);}
    	                		thisSym.setOffset(offset);
    	                		offset = offset - 4;
    	                }
    	            } else {
    	                node.nameAnalysis(symTab);
    	            } // end else
    			} // end for
    		
    }  // end nameAnalysis   
    
    /**
     * typeCheck
     */
    public void typeCheck() {
        for (DeclNode node : myDecls) {
            node.typeCheck();
        }
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen() {
        codeGen(false);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen(boolean globalFlag) {
    	for (DeclNode node : myDecls) {
    			if (node instanceof FnDeclNode) {
    				((FnDeclNode)node).codeGen();
    			}
    			if (globalFlag && node instanceof VarDeclNode) {
    				((VarDeclNode)node).codeGen(true);
    			}
        }

    		if (debug) {System.out.println("codeGen for DeclListNode complete");};
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

	public int getSize() {
		return myDecls.size();
	}
	
    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     *     process the formal decl
     *     if there was no error, add type of formal decl to list
     */
    public List<Type> nameAnalysis(SymTable symTab) {
    		
        List<Type> typeList = new LinkedList<Type>();

        for (FormalDeclNode node : myFormals) {
            Sym sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
                // handle offset
                // set sym's offset
                sym.setOffset(offset);
                if (debug) {System.out.println("Formal param: " + node.toString() + " offset: " + offset);}
                // update overall offset to account for processed param
                offset = offset - 4;
            }
        }
        return typeList;
    }    
    
    /**
     * Return the number of formals in this list.
     */
    public int length() {
        return myFormals.size();
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     */
    public void nameAnalysis(SymTable symTab) {
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
    }    
 
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myStmtList.typeCheck(retType);
    }   
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen(String exitLabel) {
        myStmtList.codeGen(exitLabel);
        if (debug) {System.out.println("codeGen for fnBodyNode complete");};
    }

          
    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }
    
	public int getSize() {
		return myDeclList.getSize();
	}
	
    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;

}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }    
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        for(StmtNode node : myStmts) {
            node.typeCheck(retType);
        }
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen(String exitLabel) {
        for (StmtNode node : myStmts) {
        		if (node instanceof ReturnStmtNode) {
        			((ReturnStmtNode)node).codeGen(exitLabel);
        		}
        		else if (node instanceof IfStmtNode) {
        			((IfStmtNode)node).codeGen(exitLabel);
        		}
        		else if (node instanceof IfElseStmtNode) {
        			((IfElseStmtNode)node).codeGen(exitLabel);
        		}
        		else if (node instanceof WhileStmtNode) {
        			((WhileStmtNode)node).codeGen(exitLabel);
        		}
        		else {
        			node.codeGen();
        		}
        }
        if (debug) {System.out.println("codeGen for StmtList Node complete");};
    }

    
    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }
    
    public int size() {
        return myExps.size();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(List<Type> typeList) {
        int k = 0;
        try {
            for (ExpNode node : myExps) {
                Type actualType = node.typeCheck();     // actual type of arg
                
                if (!actualType.isErrorType()) {        // if this is not an error
                    Type formalType = typeList.get(k);  // get the formal type
                    if (!formalType.equals(actualType)) {
                        ErrMsg.fatal(node.lineNum(), node.charNum(),
                                     "Type of actual does not match type of formal");
                    }
                }
                k++;
            }
        } catch (NoSuchElementException e) {
            System.err.println("unexpected NoSuchElementException in ExpListNode.typeCheck");
            System.exit(-1);
        }
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen() {
        Iterator<ExpNode> it = myExps.iterator();
        while (it.hasNext()) {
        		it.next().codeGen();
        }
        
        if (debug) {System.out.println("codeGen for ExpList Node complete");};
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /**
     * Note: a formal decl needs to return a sym
     */
    abstract public Sym nameAnalysis(SymTable symTab);

    abstract public void codeGen();

	// default version of typeCheck for non-function decls
    public void typeCheck() { }
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

	public int getSize() {
		return mySize;
	}

	/**
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a struct type, 
     *     lookup type name (globally)
     *     if type name doesn't exist, then error
     * if no errors so far,
     *     if name has already been declared in this scope, then error
     *     else add name to local symbol table     
     *
     * symTab is local symbol table (say, for struct field decls)
     * globalTab is global symbol table (for struct type names)
     * symTab and globalTab can be the same
     */
    public Sym nameAnalysis(SymTable symTab) {
        return nameAnalysis(symTab, symTab);
    }
    
    public Sym nameAnalysis(SymTable symTab, SymTable globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        Sym sym = null;
        IdNode structId = null;
        mySize = 4;

        if (myType instanceof VoidNode) {  // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        else if (myType instanceof StructNode) {
            structId = ((StructNode)myType).idNode();
            sym = globalTab.lookupGlobal(structId.name());
            
            // if the name for the struct type is not found, 
            // or is not a struct type
            if (sym == null || !(sym instanceof StructDefSym)) {
                ErrMsg.fatal(structId.lineNum(), structId.charNum(), 
                             "Invalid name of struct type");
                badDecl = true;
            }
            else {
                structId.link(sym);
            }
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                if (myType instanceof StructNode) {
                    sym = new StructSym(structId);
                }
                else {
                    sym = new Sym(myType.type());
                }
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } 
        }
        
        return sym;
    }    
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen(boolean globalFlag) {
        if(globalFlag) {
        		// handle global case
        		Codegen.generate(".data");
        		Codegen.generateWithComment(".align 2", " align on a word boundary");
        		Codegen.generateLabeled("_"+myId.name(), ".space ", "", int2str(mySize));
        }
        else {
        		// no action
        }
        
        if (debug) {System.out.println("codeGen for VarDeclNode complete");};
    }

    
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.println(";");
    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;

	public void codeGen() {
		
	}
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     *     enter new scope
     *     process the formals
     *     if this function is not multiply declared,
     *         update symbol table entry with types of formals
     *     process the body of the function
     *     exit scope
     */
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        FnSym sym = null;
        
        if (debug) {System.out.println("Processing function: "+name);}
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                         "Multiply declared identifier");
        }
        
        else { // add function name to local symbol table
            try {
                sym = new FnSym(myType.type(), myFormalsList.length());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                                   " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } 
        }
        
        symTab.addScope();  // add a new scope for locals and params
        
        offset = 0; // set initial offset
        
        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
        }
        
        if (debug) {System.out.println("offset after formals: " + offset);}
        
        int paramsSize = 4 * typeList.size();
        if (debug) {System.out.println("params size = "+paramsSize);}
        sym.setParamsSize(paramsSize); 
        
        // size of locals = size of body
        int localsSize = myBody.getSize();
        sym.setLocalsSize(localsSize * 4);
        if (debug) {System.out.println("locals size = "+localsSize*4);}
        
        offset = offset - 8; 
        
        if (debug) {System.out.println("offset before body: " + offset);}
        
        myBody.nameAnalysis(symTab); // process the function body
        
        if (debug) {System.out.println("offset after body: " + offset);}
        
        try {
            symTab.removeScope();  // exit scope
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in FnDeclNode.nameAnalysis");
            System.exit(-1);
        }
        
        return null;
    } 
       
    /**
     * typeCheck
     */
    public void typeCheck() {
        myBody.typeCheck(myType.type());
    }
        
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen() {
        // setup
    		String fnName = myId.name();
    		FnSym fnSym = (FnSym)myId.sym();
    		int localsSize = fnSym.getLocalsSize(); 
 	    int paramsSize = fnSym.getParamsSize();
 	   String exitLabel;
 	    if (fnName.equals("main")) {
 	    		exitLabel = "main_Exit";
 	    }
 	    else {
 	    		exitLabel = Codegen.nextLabel();
 	    }
    		
    		//
    		// function preamble
    		//
    		if (fnName.equals("main")) {
    			// handle main
    			Codegen.generate(".text");
    			Codegen.generate(".globl main");
    			Codegen.generateLabeled("main", "", " METHOD ENTRY");
    			//Codegen.generateLabeled("__start", "", " add __start label for main only");
    		}
    		else {
    			Codegen.generate(".text");
    			Codegen.generateLabeled("_"+fnName, "", " METHOD ENTRY");
    		}
    		
    		//
    		// function entry
    		//    		
    		//push the return address
    		Codegen.genPush(Codegen.RA);
    		
    		//push the control link
    		Codegen.genPush(Codegen.FP);
    		
    		//set the FP
    		Codegen.generateWithComment("addu", " set the FP", Codegen.FP, Codegen.SP, int2str(paramsSize + 8));
    		
    		//push space for local variables
    		if (localsSize > 0) {
    			Codegen.generateWithComment("subu", " push space for locals", Codegen.SP, Codegen.SP, int2str(localsSize));
    		}
    		
    		//
    		// function body
    		//
    		myBody.codeGen(exitLabel);
    		
    		//
    		// function exit
    		//
    		Codegen.generateWithComment("", "FUNCTION EXIT");
    		Codegen.genLabel(exitLabel, ""); 
    		// load return address
    		Codegen.generateIndexed("lw", Codegen.RA, Codegen.FP, -(paramsSize), " load return address");
    		// save control link
    		Codegen.generateWithComment("move", " save control link", Codegen.T0, Codegen.FP);
    		// restore FP
    		Codegen.generateIndexed("lw", Codegen.FP, Codegen.FP, -(paramsSize + 4) , " restore FP");
    		// restore SP
    		Codegen.generateWithComment("move", " restore SP", Codegen.SP, Codegen.T0);
    		// return
    		if (fnName.equals("main")) {
    			// handle main
    			Codegen.generateWithComment("li", " load exit code for syscall", Codegen.V0, int2str(10));
    			Codegen.generateWithComment("syscall", " only for main exit");
    		}
    		else {
    			Codegen.generateWithComment("jr", " return", Codegen.RA);
    		}

        if (debug) {System.out.println("codeGen for programNode complete");};
    }
    
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}\n");
    }

    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     *     then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Sym
     */
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        Sym sym = null;
        
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                sym = new Sym(myType.type());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }        
	}
        
        return sym;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
	@Override
	public void codeGen() {

	}
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     *     then multiply declared error (don't add to symbol table)
     * create a new symbol table for this struct definition
     * process the decl list
     * if no errors
     *     add a new entry to symbol table for this struct
     */
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }

        SymTable structSymTab = new SymTable();
        
        // process the fields of the struct
        myDeclList.nameAnalysis(structSymTab, symTab);
        
        if (!badDecl) {
            try {   // add entry to symbol table
                StructDefSym sym = new StructDefSym(structSymTab);
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (WrongArgumentException ex) {
                System.err.println("Unexpected WrongArgumentException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } 
        }
        
        return null;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("struct ");
        p.print(myId.name());
        p.println("{");
        myDeclList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("};\n");

    }

    // 2 kids
    private IdNode myId;
    private DeclListNode myDeclList;
	
	public void codeGen() {
		// Do not need to implement for this project		
	}
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new IntType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new BoolType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }
    
    /**
     * type
     */
    public Type type() {
        return new VoidType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public IdNode idNode() {
        return myId;
    }
    
    /**
     * type
     */
    public Type type() {
        return new StructType(myId);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        p.print(myId.name());
    }
    
    // 1 kid
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);
    abstract public void codeGen();
	abstract public void typeCheck(Type retType);
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myAssign.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myAssign.typeCheck();
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen() {
        myAssign.codeGen();
        Codegen.genPop(Codegen.T0);
        
        if (debug) {System.out.println("codeGen for assignStmtNode complete");};
    }
        
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // 1 kid
    private AssignNode myAssign;

}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
        }
    }
        
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "POSTINC");
	    
		// push address onto stack
		((IdNode)myExp).genAddr();

	    // pop value in T1
	    Codegen.genPop(Codegen.T1);
	    
	    // eval myExp
	    myExp.codeGen();
	    
	    // store in T0
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the decrement
	    Codegen.generate("add", Codegen.T0, Codegen.T0, "1");
	    
	    // step 4: process result
	    Codegen.generateIndexed("sw", Codegen.T0,  Codegen.T1, 0);
		
	}
    
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    // 1 kid
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
        }
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "POSTDEC");
	    
		// push address onto stack
		((IdNode)myExp).genAddr();

	    // pop value in T1
	    Codegen.genPop(Codegen.T1);
	    
	    // eval myExp
	    myExp.codeGen();
	    
	    // store in T0
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the decrement
	    Codegen.generate("sub", Codegen.T0, Codegen.T0, "1");
	    
	    // step 4: process result
	    Codegen.generateIndexed("sw", Codegen.T0,  Codegen.T1, 0);
		
	}
        
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }
    
    // 1 kid
    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }    
 
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (type.isFnType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read a function");
        }
        
        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read a struct name");
        }
        
        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read a struct variable");
        }
    }

    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen() {   		
    		Codegen.generateWithComment("", "READ");
    		
    		// generate special code for read node
    		Codegen.generate("li", Codegen.V0, "5");
    		Codegen.generate("syscall");

    		// store the value from V0 to the address of the IdNode
		// get address
    		((IdNode)myExp).genAddr();
		Codegen.genPop(Codegen.T0);
		
		// do the store
		Codegen.generateIndexed("sw", Codegen.V0, Codegen.T0, 0);
	}
    
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (type.isFnType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write a function");
        }
        
        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write a struct name");
        }
        
        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write a struct variable");
        }
        
        if (type.isVoidType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write void");
        }
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen() {
    		Type type = myExp.typeCheck();
    		
    		Codegen.generateWithComment("", "WRITE");
    		// call codegen on exp being printed
        myExp.codeGen();
        // pop the top-of-stack value into A0
        Codegen.genPop(Codegen.A0);
        
        if (type.isBoolType() || type.isIntType()) {
        		// set register V0 to 1
        		// only do this for ints and bools
        		Codegen.generate("li", Codegen.V0, "1");
        }
        else if (type.isStringType()) {
        		// set register V0 to 4
            Codegen.generate("li", Codegen.V0, "4");
        }
        
        // syscall instruction
        Codegen.generate("syscall");
        
        if (debug) {System.out.println("codeGen for writeStmtNode complete");};
    }
        
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
     /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-bool expression used as an if condition");        
        }
        
        myStmtList.typeCheck(retType);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen(String exitLabel) {
    		Codegen.generateWithComment("", "IF STMT");
    		
    		// generate label
    		String falseLabel = Codegen.nextLabel();
    		
    		// evaluate condition, leave value on stack
    		myExp.codeGen();
    		
    		// pop top-of stact to reg T0
    		Codegen.genPop(Codegen.T0);
    		
    		// jump to false label if T0 == FALSE
    		Codegen.generate("beq", Codegen.T0, Codegen.FALSE, falseLabel);
    		
    		// code gen for statement list
    		myStmtList.codeGen(exitLabel);
    		
    		// FalseLabel:
    		Codegen.genLabel(falseLabel);
		
	}
       
	public void codeGen() {
	}
	
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("}");
    }

    // e kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-bool expression used as an if condition");        
        }
        
        myThenStmtList.typeCheck(retType);
        myElseStmtList.typeCheck(retType);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen(String exitLabel) {
		Codegen.generateWithComment("", "IF ELSE STMT");
		
		// generate label
		String elseLabel = Codegen.nextLabel();
		String doneLabel = Codegen.nextLabel();
		
		//Evaluate the condition, leaving the value on the stack
		myExp.codeGen();
		
		//Pop the top-of-stack value into register T0
		Codegen.genPop(Codegen.T0);
		
		//Jump to ElseLabel if T0 == FALSE
		Codegen.generate("beq", Codegen.T0, Codegen.FALSE, elseLabel);
		
		//Code for the statement list in If
		myThenStmtList.codeGen(exitLabel);
		
		//Jump to DoneLabel
		Codegen.generate("b", doneLabel);
		
		//ElseLabel:
		Codegen.genLabel(elseLabel);
		
		//Code for the statement list in Else
		myElseStmtList.codeGen(exitLabel);
		
		//DoneLabel:
		Codegen.genLabel(doneLabel);
	}
	
	public void codeGen() {
	}
        
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("}");
        addIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("}");        
    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-bool expression used as a while condition");        
        }
        
        myStmtList.typeCheck(retType);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen(String exitLabel) {
		Codegen.generateWithComment("", "WHILE STMT");
		
		// generate labels
		String loopLabel = Codegen.nextLabel();
		String doneLabel = Codegen.nextLabel();
				
		//LoopLabel:
		Codegen.genLabel(loopLabel);
		
		//Evaluate the condition, leaving the value on the stack.
		myExp.codeGen();
		
		//Pop the top-of-stack value and see if it's zero; if zero, jump to DoneLab
		Codegen.genPop(Codegen.T0);
		Codegen.generate("beq", Codegen.T0, Codegen.FALSE, doneLabel);
		
		//Code for the statements in the body of the loop.
		myStmtList.codeGen(exitLabel);
		
		//Jump to LoopLabel
		Codegen.generate("b", loopLabel);
		
		//DoneLabel:
		Codegen.genLabel(doneLabel);
	}
	
	public void codeGen() {
	}
        
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class RepeatStmtNode extends StmtNode {
    public RepeatStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-integer expression used as a repeat clause");        
        }
        
        myStmtList.typeCheck(retType);
    }
        
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("repeat (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        addIndent(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
    
	public void codeGen() {
		// Not needed for this project
		
	}
}


class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myCall.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myCall.typeCheck();
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "CALL STMT");
		
		// eval call
		myCall.codeGen();
		
	}
    
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    // 1 kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     */
    public void nameAnalysis(SymTable symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        if (myExp != null) {  // return value given
            Type type = myExp.typeCheck();
            
            if (retType.isVoidType()) {
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                             "Return with a value in a void function");                
            }
            
            else if (!retType.isErrorType() && !type.isErrorType() && !retType.equals(type)){
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                             "Bad return value");
            }
        }
        
        else {  // no return value given -- ok if this is a void function
            if (!retType.isVoidType()) {
                ErrMsg.fatal(0, 0, "Missing return value");                
            }
        }
        
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen(String exitLabel) {
		Codegen.generateWithComment("", "RETURN");
		
		// eval each expression 
		if (myExp != null) {
			myExp.codeGen();
			Codegen.genPop(Codegen.V0);
		}
		
		// then jump to the return address 
		Codegen.generate("b", exitLabel);
	}
	
	public void codeGen() {
	}
    
    public void unparse(PrintWriter p, int indent) {
        addIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /**
     * Default version for nodes with no names
     */
    public void nameAnalysis(SymTable symTab) { }
	abstract public Type typeCheck();
	abstract public void codeGen();
	abstract public void genAddr();
    abstract public int lineNum();
    abstract public int charNum();
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }
    
    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }
        
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new IntType();
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen() {
        // load value into T0
        Codegen.generateWithComment("li", " load value into T0", Codegen.T0, int2str(myIntVal));
        // push onto stack
        Codegen.genPush(Codegen.T0);
        
        if (debug) {System.out.println("codeGen for IntLitNode complete");};
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }
    
    public void genAddr() {
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }
    
    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new StringType();
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen() {
    		// first lookup to see if this is an existing string literal
    		String label = Codegen.lookupStringInMap(myStrVal);
    		
    		if (label == null) {
    			// this is a new string
    			
    			// generate label
    			label = Codegen.nextLabel();
    			// store in string map
    			Codegen.addEntryToStringMap(myStrVal, label);
    	        // store stringLit in static data area
    	    		Codegen.generate(".data");
    	    		Codegen.generateLabeled(label, ".asciiz ", "", myStrVal);
    		}
    		
        // push address of StringLit onto stack
    		Codegen.generate(".text");
    		Codegen.generateWithComment("la", " load addr into $t0", Codegen.T0, label);

    		Codegen.genPush(Codegen.T0);
    		
        if (debug) {System.out.println("codeGen for StrLitNode complete");};
    }
        
    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }
    
	public void genAddr() {
	}

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen() {
        // load value into T0
        Codegen.generateWithComment("li", " load value into T0", Codegen.T0, Codegen.TRUE);
        // push onto stack

        Codegen.genPush(Codegen.T0);
        
        if (debug) {System.out.println("codeGen for TrueNode complete");};
    }
        
    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }
    
	public void genAddr() {
	}

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen() {
        // load value into T0
        Codegen.generateWithComment("li", " load value into T0", Codegen.T0, Codegen.FALSE);
        // push onto stack
        Codegen.genPush(Codegen.T0);
        
        if (debug) {System.out.println("codeGen for FalseNode complete");};
    }
    
	public void genAddr() {
	}
        
    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /**
     * Link the given symbol to this ID.
     */
    public void link(Sym sym) {
        mySym = sym;
    }
    
    /**
     * Return the name of this ID.
     */
    public String name() {
        return myStrVal;
    }
    
    /**
     * Return the symbol associated with this ID.
     */
    public Sym sym() {
        return mySym;
    }
    
    /**
     * Return the line number for this ID.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this ID.
     */
    public int charNum() {
        return myCharNum;
    }    
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     */
    public void nameAnalysis(SymTable symTab) {
        Sym sym = symTab.lookupGlobal(myStrVal);
        if (sym == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
        } else {
            link(sym);
        }
    }
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        if (mySym != null) {
            return mySym.getType();
        } 
        else {
            System.err.println("ID with null sym field in IdNode.typeCheck");
            System.exit(-1);
        }
        return null;
    }
    
    /**
     * genJumpAndLink
     * Function to generate MIPS code for this program
     * Specifically - for idNodes used in function calls
     */
    public void genJumpAndLink() {
        String label;
        if (myStrVal.equals("main")) {
        		label = "main";
        }
        else {
        		label = "_"+myStrVal;
        }
        
        // generate jump-and-link instruction
        Codegen.generate("jal", label);
        
        if (debug) {System.out.println("codeGen for IdNode-genJumpAndLink complete");};
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     * Specifically - for idNodes used in expressions
     */
    public void codeGen() {
        // different paths for local and global
    		if (mySym.getOffset() <= 0) { 
    			// local case
    			Codegen.generateIndexed("lw", Codegen.T0, Codegen.FP, 
    					mySym.getOffset(), "load the value of local into T0");
    		}
    		else {
    			// global case
    			Codegen.generateWithComment("lw", " load the value of global into T0", 
    					Codegen.T0, "_"+myStrVal);
    		}
    		
    		Codegen.genPush(Codegen.T0);  
    		
        if (debug) {System.out.println("codeGen for IdNode-codeGen complete");};
    }
    
    /**
     * genAddr
     * Function to generate MIPS code for this program
     * Specifically - for idNodes used in assignment
     */
    public void genAddr() {
    	
    		if (mySym.getOffset() > 0) { 
    			//global
    			Codegen.generate("la", Codegen.T0, "_"+myStrVal);
    		}
    		else {
    			// local
    			Codegen.generateIndexed("la", Codegen.T0, Codegen.FP, 
    					mySym.getOffset(), "load the address of local into T0");
    		}

    		Codegen.genPush(Codegen.T0);
    		
        if (debug) {System.out.println("codeGen for IdNode-genAddr complete");};
    }
           
    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySym != null) {
            p.print("(" + "[" + mySym.getOffset() +  "])");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym mySym;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;    
        myId = id;
        mySym = null;
    }

    /**
     * Return the symbol associated with this dot-access node.
     */
    public Sym sym() {
        return mySym;
    }    
    
    /**
     * Return the line number for this dot-access node. 
     * The line number is the one corresponding to the RHS of the dot-access.
     */
    public int lineNum() {
        return myId.lineNum();
    }
    
    /**
     * Return the char number for this dot-access node.
     * The char number is the one corresponding to the RHS of the dot-access.
     */
    public int charNum() {
        return myId.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the dot-access
     * - process the RHS of the dot-access
     * - if the RHS is of a struct type, set the sym for this node so that
     *   a dot-access "higher up" in the AST can get access to the symbol
     *   table for the appropriate struct definition
     */
    public void nameAnalysis(SymTable symTab) {
        badAccess = false;
        SymTable structSymTab = null; // to lookup RHS of dot-access
        Sym sym = null;
        
        myLoc.nameAnalysis(symTab);  // do name analysis on LHS
        
        // if myLoc is really an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode)myLoc;
            sym = id.sym();
            
            // check ID has been declared to be of a struct type
            
            if (sym == null) { // ID was undeclared
                badAccess = true;
            }
            else if (sym instanceof StructSym) { 
                // get symbol table for struct type
                Sym tempSym = ((StructSym)sym).getStructType().sym();
                structSymTab = ((StructDefSym)tempSym).getSymTable();
            } 
            else {  // LHS is not a struct type
                ErrMsg.fatal(id.lineNum(), id.charNum(), 
                             "Dot-access of non-struct type");
                badAccess = true;
            }
        }
        
        // if myLoc is really a dot-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a struct type, or
        // a link to the Sym for the struct type RHSid was declared to be
        else if (myLoc instanceof DotAccessExpNode) {
            DotAccessExpNode loc = (DotAccessExpNode)myLoc;
            
            if (loc.badAccess) {  // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this dot-access
            }
            else { //  no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) {  // no struct in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(), 
                                 "Dot-access of non-struct type");
                    badAccess = true;
                }
                else {  // get the struct's symbol table in which to lookup RHS
                    if (sym instanceof StructDefSym) {
                        structSymTab = ((StructDefSym)sym).getSymTable();
                    }
                    else {
                        System.err.println("Unexpected Sym type in DotAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }
        
        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }
        
        // do name analysis on RHS of dot-access in the struct's symbol table
        if (!badAccess) {
        
            sym = structSymTab.lookupGlobal(myId.name()); // lookup
            if (sym == null) { // not found - RHS is not a valid field name
                ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                             "Invalid struct field name");
                badAccess = true;
            }
            
            else {
                myId.link(sym);  // link the symbol
                // if RHS is itself as struct type, link the symbol for its struct 
                // type to this dot-access node (to allow chained dot-access)
                if (sym instanceof StructSym) {
                    mySym = ((StructSym)sym).getStructType().sym();
                }
            }
        }
    }    
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return myId.typeCheck();
    }
    
	public void genAddr() {
	}
    
    public void unparse(PrintWriter p, int indent) {
        myLoc.unparse(p, 0);
        p.print(".");
        myId.unparse(p, 0);
    }

    // 2 kids
    private ExpNode myLoc;    
    private IdNode myId;
    private Sym mySym;          // link to Sym for struct type
    private boolean badAccess;  // to prevent multiple, cascading errors
	
	public void codeGen() {
		// Do not need to implement
		
	}
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }
    
    /**
     * Return the line number for this assignment node. 
     * The line number is the one corresponding to the left operand.
     */
    public int lineNum() {
        return myLhs.lineNum();
    }
    
    /**
     * Return the char number for this assignment node.
     * The char number is the one corresponding to the left operand.
     */
    public int charNum() {
        return myLhs.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type typeLhs = myLhs.typeCheck();
        Type typeExp = myExp.typeCheck();
        Type retType = typeLhs;
        
        if (typeLhs.isFnType() && typeExp.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Function assignment");
            retType = new ErrorType();
        }
        
        if (typeLhs.isStructDefType() && typeExp.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct name assignment");
            retType = new ErrorType();
        }
        
        if (typeLhs.isStructType() && typeExp.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct variable assignment");
            retType = new ErrorType();
        }        
        
        if (!typeLhs.equals(typeExp) && !typeLhs.isErrorType() && !typeExp.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Type mismatch");
            retType = new ErrorType();
        }
        
        if (typeLhs.isErrorType() || typeExp.isErrorType()) {
            retType = new ErrorType();
        }
        
        
        
        return retType;
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
    public void codeGen() {
    		Codegen.generateWithComment("", "ASSIGN");
    		// Evaluate the right-hand-side expression, leaving the value on the stack.
    		myExp.codeGen(); // eval
    		
    		// Push the address of the left-hand-side Id onto the stack.
    		((IdNode)myLhs).genAddr();

    		// Store the value into the address.
    		Codegen.genPop(Codegen.T0);
    		Codegen.genPop(Codegen.T1);
    		Codegen.generateIndexed("sw", Codegen.T1, Codegen.T0, 0); 
    		
    		// Leave a copy of the value on the stack.
    		Codegen.genPush(Codegen.T1);

        if (debug) {System.out.println("codeGen for assignNode complete");};
    }
    
	public void genAddr() {
	}
    
    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");
    }

    // 2 kids
    private ExpNode myLhs;
    private ExpNode myExp;

}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    /**
     * Return the line number for this call node. 
     * The line number is the one corresponding to the function name.
     */
    public int lineNum() {
        return myId.lineNum();
    }
    
    /**
     * Return the char number for this call node.
     * The char number is the one corresponding to the function name.
     */
    public int charNum() {
        return myId.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    }  
      
    /**
     * typeCheck
     */
    public Type typeCheck() {
        if (!myId.typeCheck().isFnType()) {  
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Attempt to call a non-function");
            return new ErrorType();
        }
        
        FnSym fnSym = (FnSym)(myId.sym());
        
        if (fnSym == null) {
            System.err.println("null sym for Id in CallExpNode.typeCheck");
            System.exit(-1);
        }
        
        if (myExpList.size() != fnSym.getNumParams()) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Function call with wrong number of args");
            return fnSym.getReturnType();
        }
        
        myExpList.typeCheck(fnSym.getParamTypes());
        return fnSym.getReturnType();
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		// setup
		Codegen.generateWithComment("", "FUNCTION CALL");
		
		
		FnSym fnSym = (FnSym)(myId.sym());
		
		// call codeGen on ExpListNode
		if (myExpList != null) {
			myExpList.codeGen();
		}
		
		// call genJumpAndLink for IdNode
		myId.genJumpAndLink();
		
		// push returned value onto stack if not void type
		if (!fnSym.getReturnType().isVoidType()) {
			Codegen.genPush(Codegen.V0);
		}

	}
    
	public void genAddr() {
	}
        
    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * Return the line number for this unary expression node. 
     * The line number is the one corresponding to the  operand.
     */
    public int lineNum() {
        return myExp.lineNum();
    }
    
    /**
     * Return the char number for this unary expression node.
     * The char number is the one corresponding to the  operand.
     */
    public int charNum() {
        return myExp.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }
    
    /**
     * Return the line number for this binary expression node. 
     * The line number is the one corresponding to the left operand.
     */
    public int lineNum() {
        return myExp1.lineNum();
    }
    
    /**
     * Return the char number for this binary expression node.
     * The char number is the one corresponding to the left operand.
     */
    public int charNum() {
        return myExp1.charNum();
    }
    
	public void genAddr() {
	}
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }
    
    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new IntType();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "UNARY MINUS");
	    // step 1: evaluate operand
	    myExp.codeGen();

	    // step 2: pop value in T0
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the operation (negate)
	    Codegen.generate("neg", Codegen.T0, Codegen.T0);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}
	
	public void genAddr() {
	}

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new BoolType();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		
		Codegen.generateWithComment("", "NOT NODE");
		
	    // step 1: evaluate operand
	    myExp.codeGen();

	    // step 2: pop value in T0
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do operation (use seq over not per documentation)
	    Codegen.generate("seq", Codegen.T0, Codegen.T0, Codegen.FALSE);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}
	
	public void genAddr() {
	}

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

abstract class ArithmeticExpNode extends BinaryExpNode {
    public ArithmeticExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new IntType();
        
        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class LogicalExpNode extends BinaryExpNode {
    public LogicalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();
        
        if (!type1.isErrorType() && !type1.isBoolType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isBoolType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class EqualityExpNode extends BinaryExpNode {
    public EqualityExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();
        
        if (type1.isVoidType() && type2.isVoidType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to void functions");
            retType = new ErrorType();
        }
        
        if (type1.isFnType() && type2.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to functions");
            retType = new ErrorType();
        }
        
        if (type1.isStructDefType() && type2.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to struct names");
            retType = new ErrorType();
        }
        
        if (type1.isStructType() && type2.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to struct variables");
            retType = new ErrorType();
        }        
        
        if (!type1.equals(type2) && !type1.isErrorType() && !type2.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Type mismatch");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class RelationalExpNode extends BinaryExpNode {
    public RelationalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();
        
        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

class PlusNode extends ArithmeticExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "ADDITION");
	    // step 1: evaluate both operands
	    myExp1.codeGen();
	    myExp2.codeGen();

	    // step 2: pop values in T0 and T1
	    Codegen.genPop(Codegen.T1);
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the addition (T0 = T0 + T1)
	    Codegen.generate("addu", Codegen.T0, Codegen.T0, Codegen.T1);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}
}

class MinusNode extends ArithmeticExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "SUBTRACTION");
	    // step 1: evaluate both operands
	    myExp1.codeGen();
	    myExp2.codeGen();

	    // step 2: pop values in T0 and T1
	    Codegen.genPop(Codegen.T1);
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the subtraction (T0 = T0 - T1)
	    Codegen.generate("subu", Codegen.T0, Codegen.T0, Codegen.T1);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends ArithmeticExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "MULTIPLICATION");
	    // step 1: evaluate both operands
	    myExp1.codeGen();
	    myExp2.codeGen();

	    // step 2: pop values in T0 and T1
	    Codegen.genPop(Codegen.T1);
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the multiplication (T0 = T0 * T1)
	    Codegen.generate("mul", Codegen.T0, Codegen.T0, Codegen.T1);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends ArithmeticExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "DIVISION");
	    // step 1: evaluate both operands
	    myExp1.codeGen();
	    myExp2.codeGen();

	    // step 2: pop values in T0 and T1
	    Codegen.genPop(Codegen.T1);
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the division (T0 = T0 / T1)
	    Codegen.generate("divu", Codegen.T0, Codegen.T0, Codegen.T1);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends LogicalExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		// create false label
		String falseLab = Codegen.nextLabel();
		Codegen.generateWithComment("", "AND NODE");
		
		// evaluate the left operand
		myExp1.codeGen();
		
		// if false, jump to false label
		Codegen.genPop(Codegen.T0);
		Codegen.generate("beq", Codegen.T0, Codegen.FALSE, falseLab);
		
		// if the value is true then evaluate the right operand;
		myExp2.codeGen();
		// that value is the value of the whole expression
		Codegen.genPop(Codegen.T0); // get value
		Codegen.generate("beq", Codegen.T0, Codegen.FALSE, falseLab);
		
		// else, don't bother to evaluate the right operand
		// the value of the whole expression is false
		Codegen.genLabel(falseLab);
		
		Codegen.genPush(Codegen.T0);
		
	}
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends LogicalExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		// create true label
		String trueLab = Codegen.nextLabel();
		Codegen.generateWithComment("", "OR NODE");
		
		// evaluate the left operand
		myExp1.codeGen();
		
		// if true (1), jump to true label
		Codegen.genPop(Codegen.T0);
		Codegen.generate("beq", Codegen.T0, Codegen.TRUE, trueLab);
		
		// if the value is false then evaluate the right operand;
		myExp2.codeGen();
		// that value is the value of the whole expression
		Codegen.genPop(Codegen.T0); // get value
		Codegen.generate("beq", Codegen.T0, Codegen.TRUE, trueLab);
		
		// else, don't bother to evaluate the right operand
		// the value of the whole expression is true
		Codegen.genLabel(trueLab);
		
		Codegen.genPush(Codegen.T0);
		
	}
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends EqualityExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "EQUALS");
	    // step 1: evaluate both operands
	    myExp1.codeGen();
	    myExp2.codeGen();

	    // step 2: pop values in T0 and T1
	    Codegen.genPop(Codegen.T1);
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the operation 
	    Codegen.generate("seq", Codegen.T0, Codegen.T0, Codegen.T1);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends EqualityExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "NOT EQUALS");
	    // step 1: evaluate both operands
	    myExp1.codeGen();
	    myExp2.codeGen();

	    // step 2: pop values in T0 and T1
	    Codegen.genPop(Codegen.T1);
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the operation 
	    Codegen.generate("sne", Codegen.T0, Codegen.T0, Codegen.T1);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends RelationalExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "LESS");
	    // step 1: evaluate both operands
	    myExp1.codeGen();
	    myExp2.codeGen();

	    // step 2: pop values in T0 and T1
	    Codegen.genPop(Codegen.T1);
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the operation 
	    Codegen.generate("slt", Codegen.T0, Codegen.T0, Codegen.T1);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends RelationalExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "GREATER");
	    // step 1: evaluate both operands
	    myExp1.codeGen();
	    myExp2.codeGen();

	    // step 2: pop values in T0 and T1
	    Codegen.genPop(Codegen.T1);
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the operation 
	    Codegen.generate("sgt", Codegen.T0, Codegen.T0, Codegen.T1);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends RelationalExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "LESS THAN OR EQUAL TO");
	    // step 1: evaluate both operands
	    myExp1.codeGen();
	    myExp2.codeGen();

	    // step 2: pop values in T0 and T1
	    Codegen.genPop(Codegen.T1);
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the operation 
	    Codegen.generate("sle", Codegen.T0, Codegen.T0, Codegen.T1);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}
	
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends RelationalExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * codeGen
     * Function to generate MIPS code for this program
     */
	public void codeGen() {
		Codegen.generateWithComment("", "GREATER THAN OR EQUAL TO");
	    // step 1: evaluate both operands
	    myExp1.codeGen();
	    myExp2.codeGen();

	    // step 2: pop values in T0 and T1
	    Codegen.genPop(Codegen.T1);
	    Codegen.genPop(Codegen.T0);
	    
	    // step 3: do the operation 
	    Codegen.generate("sge", Codegen.T0, Codegen.T0, Codegen.T1);
	    
	    // step 4: push result
	    Codegen.genPush(Codegen.T0);
	}

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
