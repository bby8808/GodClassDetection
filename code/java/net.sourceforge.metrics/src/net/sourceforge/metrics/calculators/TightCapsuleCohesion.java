package net.sourceforge.metrics.calculators;


import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import net.sourceforge.metrics.core.Constants;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;


/**
 * 
 * 
 * @author lichao
 */
//

/*
 * */
public class TightCapsuleCohesion  extends Calculator implements Constants {

    public TightCapsuleCohesion () {
		super(TCC);
	}
    public  Map<IMethodBinding, List<IVariableBinding>> mapOfMethodAndAtrributeBinding = null ;
    public  IMethodBinding currentMethod = null;
    public  IType type =null;
    public static ITypeBinding currentClass = null;
    @Override
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
    	if (source.getLevel() != TYPE) {
			throw new InvalidSourceException("Tight Capsule Cohesion is only applicable to types");
		}
    	mapOfMethodAndAtrributeBinding = new HashMap<>();
		type = ((IType) source.getJavaElement());
		Visitor visitor = new Visitor();
		ICompilationUnit compilationUnit =type.getCompilationUnit();
		ASTParser astParser = ASTParser.newParser(AST.JLS8);  
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        astParser.setResolveBindings(true);
        astParser.setBindingsRecovery(true);
        astParser.setSource(compilationUnit);  
        CompilationUnit unit = (CompilationUnit) (astParser.createAST(null));
		unit.accept(visitor);
		int relativeNumberOfMethodPairs = 0;
		int totalNumberOfMethods = mapOfMethodAndAtrributeBinding.size();
		if(totalNumberOfMethods == 0){
			source.setValue(new Metric(TCC, 0));
			return;
		}
		Set<IMethodBinding> Methods = mapOfMethodAndAtrributeBinding.keySet();
		IMethodBinding[] nameOfMethods = Methods.toArray(new IMethodBinding[Methods.size()]);
		for(int i = 0; i < nameOfMethods.length; ++i){
			for(int j = i + 1; j < nameOfMethods.length; ++j){
				IMethodBinding oneName = nameOfMethods[i];
		     	IMethodBinding anotherName = nameOfMethods[j];
				List<IVariableBinding> one = mapOfMethodAndAtrributeBinding.get(oneName);
				List<IVariableBinding> two = mapOfMethodAndAtrributeBinding.get(anotherName);
				if(one.size() == 0 || two.size() == 0)//其中一个方法所使用本类的属性的个数为0，不用比较
					continue;
				//遍历每个list,循环比较
				boolean ok =false;
				for(Iterator<IVariableBinding>itOne = one.iterator(); itOne.hasNext(); ){
					if(ok == true){
						break;
					}
					IVariableBinding one1 = itOne.next();
					for(Iterator<IVariableBinding>itTwo = two.iterator(); itTwo.hasNext(); ){
						IVariableBinding two1 = itTwo.next();
						if(one1.equals(two1)){
							++relativeNumberOfMethodPairs;
							ok = true;
							break;
						}
					}
				}
			}
		}
	//	System.out.print(totalNumberOfMethods+"   "+relativeNumberOfMethodPairs+" @@@@  \n");
		if (total(totalNumberOfMethods)!=0)
		{
			double tcc =((long)relativeNumberOfMethodPairs) * 1.0 / total(totalNumberOfMethods);
			source.setValue(new Metric(TCC, tcc));
		}else
			source.setValue(new Metric(TCC, 0));
    }
    public static long total(int n){
		//计算总方法对数n*(n-1)/2
		long ans;
		if(n % 2 == 1){
			ans =  (n - 1) / 2 * n;
		}else{
			ans = n / 2 * (n - 1);
		}
		return ans;
	}
    
	public class Visitor extends ASTVisitor {
		
		public boolean visit(PackageDeclaration node){
			return false;
		}
		
		public boolean visit(ImportDeclaration node){
			return false;
		}
		
		public boolean visit(FieldDeclaration node){
			
			
			return false;
		}
		
		public boolean visit(SuperFieldAccess node){
			return false;
		}
		
		public boolean visit(SuperConstructorInvocation node){
			return false;
		}
		
		public boolean visit(SuperMethodInvocation node){
			return false;
		}
	
	
		public boolean visit(TypeDeclaration node){
//			System.out.println("\nClass:\t" + node.getName());
			currentClass = node.resolveBinding();
			return true;
		}
		
		public boolean visit(MethodDeclaration node) {
			if(node.isConstructor() || (node.getModifiers() & Modifier.ABSTRACT) != 0)
				return false;
			else{
				currentMethod = node.resolveBinding();
				
				List<IVariableBinding> list = new ArrayList<>(); 
				mapOfMethodAndAtrributeBinding.put(currentMethod, list);
				return true;
			}
		}
		
		public  boolean visit(SimpleName node){
//			System.out.println("\nSimpleName:\t" + node);
			IBinding binding = node.resolveBinding();
			if(binding instanceof IVariableBinding){
				IVariableBinding variableBinding = (IVariableBinding) binding;
				if(variableBinding.isField()){
					ITypeBinding fromClass = variableBinding.getDeclaringClass();
					if(fromClass == null)
						return true;
					if(fromClass.equals(currentClass)){
						
						List<IVariableBinding> list = mapOfMethodAndAtrributeBinding.get(currentMethod);
						if ((list!=null))
						{
							if((! list.contains(variableBinding))){
								list.add(variableBinding);
							}
						}else
						{
						
							list = new ArrayList<IVariableBinding>();
							list.add(variableBinding);
							
						}
					}
				}
			}
			return true;
		}
		
	}
}
