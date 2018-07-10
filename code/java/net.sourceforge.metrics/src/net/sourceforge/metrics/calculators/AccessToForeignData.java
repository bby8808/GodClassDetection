package net.sourceforge.metrics.calculators;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

public class AccessToForeignData extends Calculator {

	private TypeDeclaration type ;
	private ITypeBinding currentClass ;
	private int  tipos;
	
	private List<String> srcPackages;
	private ArrayList<IBinding> havaAccess ;
	
	private IBinding vField=null;
	//luohui
	private HashSet<String> set;
	//luohui
	public AccessToForeignData() {
		super(ATFD);
	}
	/**
	 * @see net.sourceforge.metrics.calculators.Calculator#calculate(net.sourceforge.metrics.core.sources.AbstractMetricSource)
	 */
	@Override
	public void calculate(AbstractMetricSource source) throws InvalidSourceException {
		if (source.getLevel() != TYPE) {
			throw new InvalidSourceException("Access To Foreign Data is only applicable to types");
		}
		//luohui
		set = new HashSet<String>();
		//luohui
		tipos = 0;
		
		srcPackages = new ArrayList<String>();
		havaAccess = new ArrayList<IBinding>();
		
		currentClass = null;
		if(source.getASTNode() instanceof TypeDeclaration){
		    type =  (TypeDeclaration)source.getASTNode();
		    currentClass = type.resolveBinding();
			IJavaProject project = source.getJavaElement().getJavaProject();
			IPackageFragment[] fragments;
			try {
				fragments = project.getPackageFragments();
				for(IPackageFragment fragment : fragments){
			        if(fragment.getKind() != IPackageFragmentRoot.K_SOURCE)
			        	continue;
			        srcPackages.add(fragment.getElementName());
			    }
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (type == null) {
				source.setValue(getZero());
			}
			ForeignDataCounter lc = new ForeignDataCounter();
			type.accept(lc);
		}

		source.setValue(new Metric(ATFD,tipos));
	
	}

	private class ForeignDataCounter extends ASTVisitor {
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
		public boolean visit(QualifiedName node){
			if(node.resolveBinding() instanceof IVariableBinding){
				IVariableBinding variableBinding = (IVariableBinding) node.resolveBinding();				
				if(variableBinding.isField() ){          //&& (variableBinding.getModifiers() & Modifier.FINAL) == 0
					ITypeBinding typeBinding = variableBinding.getDeclaringClass();
					if(typeBinding== null)
						return true;
					if(!typeBinding.equals(currentClass)){
						for(String srcPackage : srcPackages){
							if((!havaAccess.contains(node.resolveBinding()))&&srcPackage.equals(typeBinding.getPackage().getName())){
								havaAccess.add(node.resolveBinding());
								//luohui
								//System.out.println("--------------------------luohui----------------------------------");
								//System.out.println(typeBinding.getQualifiedName());
								set.add(typeBinding.getQualifiedName());
								//luohui
								try {
									if ((((IField)variableBinding.getJavaElement()).getFlags()& Flags.AccFinal) == 0)//(node.getFlags()& Flags.AccFinal )== 0)			
									     tipos++;
								} catch (JavaModelException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}	
								break;
							}
						}
					}
				}
				
			}
			return true;
		}
		
		public boolean visit(FieldAccess node){
			if(node.resolveFieldBinding() == null){
				return true;
			}
			ITypeBinding typeBinding = node.resolveFieldBinding().getDeclaringClass();
			if(typeBinding == null)
				return true;
			if(!typeBinding.equals(currentClass)){ 
				for(String srcPackage : srcPackages){
					if((!havaAccess.contains(node.resolveFieldBinding()))&&srcPackage.equals(typeBinding.getPackage().getName())){
						havaAccess.add(node.resolveFieldBinding());
						//luohui
						//System.out.println("--------------------------luohui----------------------------------");
						//System.out.println(typeBinding.getQualifiedName());
						set.add(typeBinding.getQualifiedName());
						//luohui
						try {
							if ((((IField)node.resolveFieldBinding().getJavaElement()).getFlags()& Flags.AccFinal) == 0)
								tipos++;
						} catch (JavaModelException e) {
							// TODO Auto-generated catch block
			//				e.printStackTrace();
						}	
						break;
					}
				}				
			}
			return true;
		}
		
		public boolean visit(MethodInvocation node){
			if(isGetterOrSetter(node)){
				ITypeBinding typeBinding = node.resolveMethodBinding().getDeclaringClass();
				if(typeBinding == null)
					return true;
				if(!typeBinding.equals(currentClass)){
					for(String srcPackage : srcPackages){
						if(vField!=null&&(!havaAccess.contains(vField))&&srcPackage.equals(typeBinding.getPackage().getName())){
							havaAccess.add(vField);
							//luohui
							//System.out.println("--------------------------luohui----------------------------------");
							//System.out.println(typeBinding.getQualifiedName());
							set.add(typeBinding.getQualifiedName());
							//luohui
							tipos++;	
							break;
						}
					}
				}
				
			}
			return true;
		}
		
	}	
	public  boolean isGetterOrSetter(MethodInvocation node){
		IMethodBinding binding = node.resolveMethodBinding();
		if(binding==null)
			return false;
		String methodName = binding.getName();
		if(methodName.length() <= 3)
			return false;
		if(methodName.startsWith("get")){
			String targetField = methodName.substring(3);
			IVariableBinding[] fields = node.resolveMethodBinding().getDeclaringClass().getDeclaredFields();
			for(IVariableBinding field : fields){
				if(field.getName().equalsIgnoreCase(targetField) && field.getType().equals(node.resolveMethodBinding().getReturnType()))
				{
					vField = field;
					return true;
				}
			}
		}
		if(methodName.startsWith("set")){
			String targetField = methodName.substring(3);
			IVariableBinding[] fields = node.resolveMethodBinding().getDeclaringClass().getDeclaredFields();
			for(IVariableBinding field : fields)
				if(field.getName().equalsIgnoreCase(targetField)){
					ITypeBinding[] parameterTypes = node.resolveMethodBinding().getParameterTypes();
					if(parameterTypes.length == 1 && field.getType().equals(parameterTypes[0]))
					{
						vField = field;
						return true;
					}
				}
		}
		return false;
	}
}
	
