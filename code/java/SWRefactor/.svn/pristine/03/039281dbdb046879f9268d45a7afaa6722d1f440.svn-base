package swr.actions.combine.test;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

public class PrintTestMethods {
	public static void testSuperTypesOrClasses(IType mainType1) throws JavaModelException{
		/*------测试getAllSupertypes与getAllSuperclasses的区别-------*/
		/*------getAllSuperclasses只包括父类，getAllSupertypes还包括接口------*/
		ITypeHierarchy typeHierarchy1 = mainType1.newTypeHierarchy((IJavaProject) mainType1.getAncestor(IJavaElement.JAVA_PROJECT), null);
		IType[] superTypes1 = typeHierarchy1.getAllSupertypes(mainType1);
		IType[] superClasses1 = typeHierarchy1.getAllSuperclasses(mainType1);
		System.out.println("super types:");
		for(int i=0; i<superTypes1.length; i++){
			System.out.println(superTypes1[i].getFullyQualifiedName());
		}
		System.out.println("super classes");
		for(int i=0; i<superClasses1.length; i++){
			System.out.println(superClasses1[i].getFullyQualifiedName());
		}
	}
	
	public static void testTypeHierarchy(IType mainType1, IType mainType2) throws JavaModelException{
		/*------测试两个类可否共用一个type hierarchy，两种情况：两个类在同一继承结构中，或不在同一继承结构中-------*/
		/*------不可以, superType追溯到object------*/
		ITypeHierarchy typeHierarchy1 = mainType1.newTypeHierarchy((IJavaProject) mainType1.getAncestor(IJavaElement.JAVA_PROJECT), null);
		IType[] superTypes1 = typeHierarchy1.getAllSupertypes(mainType1);
		System.out.println(mainType1.getElementName()+"的父类们：");
		for(int i=0; i<superTypes1.length; i++){
			System.out.println(superTypes1[i].getFullyQualifiedName());
		}
		IType[] superTypes2 = typeHierarchy1.getAllSupertypes(mainType2);
		System.out.println(mainType2.getElementName()+"的父类们：");
		for(int i=0; i<superTypes2.length; i++){
			System.out.println(superTypes2[i].getFullyQualifiedName());
		}
		System.out.println("结束");
	}
}
