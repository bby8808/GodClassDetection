package swr.actions.combine;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CreateFieldOperation;
import org.eclipse.jdt.internal.core.CreateMethodOperation;

import swr.actions.aider.ActionsAider;
import swr.actions.combine.search.SearchForText;
import swr.actions.combine.search.SearchForText.Result;

@SuppressWarnings("restriction")
public class PreCombination {
	private ICompilationUnit unit1;
	private IType mainType1;
	private IField[] fields1;
	private IMethod[] methods1;
	private boolean needDefaultConstructor;
	private Result searchResult;
	
	public PreCombination(ICompilationUnit unit, IProgressMonitor monitor){
		this.unit1 = unit;
		this.mainType1 = ActionsAider.getMainType(unit1);
		this.needDefaultConstructor = checkDefaultConstructor(unit1, monitor);
		try {
			this.fields1 = mainType1.getFields();
			this.methods1 = mainType1.getMethods();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean checkDefaultConstructor(ICompilationUnit unit,IProgressMonitor monitor){
		String fullName = "";
		if(unit.getParent() instanceof IPackageFragment){
			if(!((IPackageFragment)unit.getParent()).isDefaultPackage()){
				fullName = unit.getParent().getElementName()+"."+unit.getElementName();
			}
			else
				fullName = unit.getElementName();
			fullName = fullName.substring(0, fullName.length()-5);
			fullName += "()";
		}
		SearchForText search = new SearchForText();
		return search.SearchForDefaultConstructor(fullName, unit.getJavaProject(),monitor);
	}
	
	public boolean preCombine(ICompilationUnit unit2,IProgressMonitor monitor) throws Exception{
		boolean combinable = true;
		
		if(unit1.getPath().equals(unit2.getPath())){
			return false;
		}
		
		IImportDeclaration[] imports1 = unit1.getImports();
		IImportDeclaration[] imports2 = unit2.getImports();
		if(checkImportLastFragment(imports1, imports2)){
			return false;
		}
		
		
		IType mainType2 = ActionsAider.getMainType(unit2);

		if(CombineAction.isGeneric(mainType1) && CombineAction.isGeneric(mainType2)){
			return false;
		}
		
		if(!isBothAbstractCondition(mainType1, mainType2)){
			return false;
		}

		if(checkFieldCollision(mainType2)){
			return false;
		}

		if(checkMethodCollision(mainType2)){
			return false;
		}
		
		if((mainType1.getSuperclassName()!=null)&&(mainType2.getSuperclassName()!=null))
			return false;
		
//		IType superType1 = getSuperType(mainType1);
//		IType superType2 = getSuperType(mainType2);
//		if(!superType1.getFullyQualifiedName().equals("java.lang.Object")) {
//			if(checkConstructor1(mainType2, superType1))
//				return false;
//		}else if(!superType2.getFullyQualifiedName().equals("java.lang.Object")) {
//			if(checkConstructor1(mainType1, superType2))
//				return false;
//		}
		
		ArrayList<IMethod> constructors1 = new ArrayList<IMethod>(1);
		for(IMethod method1:methods1)
			if(method1.isConstructor())
				constructors1.add(method1);
		
		ArrayList<IMethod> constructors2 = new ArrayList<IMethod>(1);
		for(IMethod method1:mainType2.getMethods())
			if(method1.isConstructor())
				constructors2.add(method1);

		if(mainType1.getSuperclassName()!=null && !constructors1.isEmpty())
			if(!checkConstructor1(constructors1,mainType2))
				return false;
		
		if(mainType2.getSuperclassName()!=null && !constructors2.isEmpty())
			if(!checkConstructor1(constructors2,mainType1))
				return false;
		
		if(needDefaultConstructor && !checkSameDefaultConstructor(constructors2))
			return false;
		
		if(checkDefaultConstructor(unit2,monitor) && !checkSameDefaultConstructor(constructors1))
			return false;
		
//		//2. 如果class1有父类的话，class2就不能有跟其父类不一样的构造函数
//		if(checkConstructor2(mainType1, mainType2))
//			return false;
//		if(checkConstructor2(mainType2, mainType1))
//			return false;
		//checkConstructor(mainType1, mainType2);
		return combinable; 
	}
	
	private boolean checkSameDefaultConstructor(ArrayList<IMethod> constructors2) throws JavaModelException{
		for(IMethod method2: constructors2){
			if(method2.getParameters().length==0){
				return true;
			}
		}
		if(constructors2.isEmpty())
			return true;
		return false;
	}
	
	private boolean checkConstructor1(ArrayList<IMethod> constructors1, 
			IType type2) throws Exception{
		for(IMethod method2: type2.getMethods()){
			if(method2.isConstructor()){
				boolean flags = false;
				for(IMethod constructor1:constructors1){
					if(constructor1.getParameterTypes().equals(method2.getParameterTypes())){
						flags = true;
						break;
					}
				}
					
				if(flags==false)
					return false;
			}

		}

		return true;
	}
	
	public boolean preCombine(ICompilationUnit unit2, IPackageFragment pack, IProgressMonitor monitor) throws Exception{
		boolean combinable = true;
		
		if(preCombine(unit2,monitor)==false)
			return false;
		
		if(checkDefaulPackage(unit1,pack)==false)
			return false;
		
		if(checkDefaulPackage(unit2, pack)==false)
			return false;
		
		if(checkMembersVisible(unit1, pack)==false)
			return false;
		
		if(checkMembersVisible(unit2, pack)==false)
			return false;
		
		return combinable;
	}
	
	private boolean checkDefaulPackage(ICompilationUnit unit, IPackageFragment pack){
		if(unit.getParent()==pack){
			//System.out.println("unit"+unit.getElementName()+"在包"+pack.getElementName()+"内");
			return true;
		}
		
		if(pack.isDefaultPackage()){
			return false;
		}
		
		return true;
	}
	
	private boolean checkMembersVisible(ICompilationUnit unit, IPackageFragment pack) throws JavaModelException{
		if(unit.getParent()==pack){
			//System.out.println("unit"+unit.getElementName()+"在包"+pack.getElementName()+"内");
			return true;
		}
		
		IType mainType1 = ActionsAider.getMainType(unit);
		IField[] fields1 = mainType1.getFields();
		for(IField field:fields1){
			if(Flags.isProtected(field.getFlags()) || Flags.isPackageDefault(field.getFlags())){
				//System.out.println(field.getSource());
				//System.out.println("错的作用域");
				return false;
			}
		}
		
		IMethod[] methods = mainType1.getMethods();
		for(IMethod method:methods){
			if(Flags.isProtected(method.getFlags()) || Flags.isPackageDefault(method.getFlags())){
				//System.out.println(method.getSource());
				//System.out.println("错的作用域");
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isBothAbstractCondition(IType type1, IType type2) throws JavaModelException{
		if((type1.getFlags() & Flags.AccAbstract)!=0){
			if((type2.getFlags() & Flags.AccAbstract) !=0)
				return true;
			return false;
		}else{
			if((type2.getFlags() & Flags.AccAbstract) !=0)
				return false;
			return true;
		}
	}
	
	private boolean checkImportLastFragment(IImportDeclaration[] imports1, IImportDeclaration[] imports2){
		for(IImportDeclaration import1: imports1){
			String longStr1 = import1.getElementName();
			String shortStr1 = longStr1.substring(longStr1.lastIndexOf('.')+1);
			for(IImportDeclaration import2: imports2){
				String longStr2 = import2.getElementName();
				String shortStr2 = longStr2.substring(longStr2.lastIndexOf('.')+1);
				
				if(shortStr1.equals(shortStr2)){
					return true;
				}
				
			}
		}
		return false;
	}
	
	private boolean checkFieldCollision(IType mainType2) throws JavaModelException{
		for(IField field1:fields1){
			CreateFieldOperation createField = new CreateFieldOperation(mainType2, field1.getSource(), false);
			IJavaModelStatus status = createField.verify();
			//System.out.println(status.getMessage());
			if(status.getCode() == IJavaModelStatusConstants.NAME_COLLISION){
				return true;
			}
		}
		return false;
	}
	
	private boolean checkMethodCollision(IType mainType2) throws JavaModelException{
		for(IMethod method1:methods1){
			CreateMethodOperation createMethod = new CreateMethodOperation(mainType2, method1.getSource(), false);
			IJavaModelStatus status = createMethod.verify();
			if(status.getCode() == IJavaModelStatusConstants.NAME_COLLISION){
				return true;
			}
		}
		return false;
	}
}
