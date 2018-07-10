package swr.actions.combine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.internal.core.text.PatternConstructor;

import swr.actions.combine.search.SearchForText;
import swr.actions.combine.search.SearchForText.Result;

public class Combination {
	//private ICompilationUnit newUnit;
	private int combineSize;
	private CombinationPiece piece;
	//private ICompilationUnit unit1;
//	private ICompilationUnit unit2;
	private ICompilationUnit[] units;
//	private IType mainType1;
//	private IType mainType2;
	private IType[] mainTypes = new IType[combineSize];

	public Combination(ICompilationUnit[] units, IType[] mainTypes){
		this.combineSize = units.length;
		this.units = units;
		this.mainTypes = mainTypes;
	}
	
	public CombinationPiece createNewCompilationUnit(IProject project, String className, 
			IPackageFragment newPack,int count,IProgressMonitor monitor) throws Exception{
		piece = new CombinationPiece(newPack, this.combineSize);
		
		String newClassName = className+".java";
		String newPackCode = newPack.getElementName();
		ICompilationUnit newUnit = newPack.createCompilationUnit(newClassName, "", false, monitor);
		
		if(!newPack.isDefaultPackage()){
			//System.out.println(newPackCode);
			newUnit.createPackageDeclaration(newPackCode, null);
		}

		//IPackageFragment[] packs = new IPackageFragment[this.combineSize];
		//String[] packCodes = new String[this.combineSize];
		for(int i=0; i<this.combineSize; i++){
			IPackageFragment pack = (IPackageFragment) units[i].getParent();
			if(pack.isDefaultPackage()){
				piece.setFormerClass(units[i].getElementName());
			}else{
				String packCode = pack.getElementName();
				piece.setFormerClass(packCode+"."+units[i].getElementName());
				newUnit.createImport(packCode+".*", null, monitor);
			}
		}

		ArrayList<IImportDeclaration> allImports = getImports(units);
		for(IImportDeclaration impo:allImports){
			String impoCode = impo.getElementName();
			newUnit.createImport(impoCode, null, monitor);
		}
		
		createNewType(className, newUnit,monitor);
		
		if(!newUnit.exists())
			return null;
		
		if(hasErrorMarkForUnit(newUnit,monitor)){
			System.out.println("merge error!");
//			throw new Exception();
			return null;
		}

		piece.setUnit(newUnit);
		piece.setElementName(newUnit.getElementName());
		
		IProject newProject = getNewProject(project, "NewProject"+count, monitor);
		newProject = buildNewProject(newProject, piece, monitor);
		if(buildProject(newProject, monitor)){
			System.out.println("build new project error!");
			deleteCompilationUnit(newUnit,monitor);
			newProject.clearHistory(monitor);
			newProject.delete(true, monitor);
//			throw new Exception();
			return null;
		}
		
		piece.setNewProject(newProject);
		
		newUnit.close();
		return piece;
	}
	
	
	private boolean buildProject(IProject project, IProgressMonitor pm) throws Exception {
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, pm);	
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, pm);
			IMarker[] markers = null;
			markers = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			for (IMarker marker: markers) {
				Integer severityType = (Integer) marker.getAttribute(IMarker.SEVERITY);
				if (severityType.intValue() == IMarker.SEVERITY_ERROR) {
					CombineAction.error_count++;
					if(CombineAction.error_count>CombineAction.k)
						throw new Exception();
					
					return true;
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}


	private IPackageFragment getPackageInNewProject(IJavaProject javaProject, 
			CombinationPiece piece) throws JavaModelException{
		return javaProject.findPackageFragment(
				javaProject.getPath().append(piece.getPack().getPath().removeFirstSegments(1)));
	}
	
	public IProject getNewProject(IProject oldProject, String newProjectName,IProgressMonitor monitor) throws CoreException{
		IProjectDescription oldDes = oldProject.getDescription();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		
		IPath rootpath = root.getLocation().append(newProjectName);
		//IProjectDescription description = workspace.newProjectDescription("NewProject");
		if(root.exists(rootpath))
			root.findMember(rootpath).delete(true, monitor);
		oldDes.setLocation(rootpath);

		oldDes.setName(newProjectName);
		oldProject.copy(oldDes, true, monitor);
		IProject newProject = root.getProject(newProjectName);
		return newProject;
	}
	
	public IProject buildNewProject(IProject newProject, CombinationPiece piece, IProgressMonitor monitor) throws Exception{
		//deleteOtherCompilationUnitInNewProject(javaProject, pieces, index);
		//System.out.println("delete other cu in "+javaProject.getElementName());
		String newText = piece.getElementName().substring(0, 
				piece.getElementName().length()-5);
		IPackageFragment pack = piece.getPack();
		String[] fullNames = piece.getFormerClasses();
		for(int i=0; i<piece.getCombineSize(); i++){
			String fullName1 = fullNames[i];
			String shortName1 = fullName1.substring(fullName1.lastIndexOf('.')+1);
			replaceToken(fullName1, shortName1, newText, pack,newProject, monitor);
			
			newProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			newProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
			IJavaProject javaProject = JavaCore.create(newProject);
			
			String pathStr = fullName1.replace('.',	IPath.SEPARATOR)+".java";
			IPath path = new Path(pathStr);
			IJavaElement e = javaProject.findElement(path);
			if(e instanceof ICompilationUnit){
				//System.out.println(e.getElementName());
				if(e.exists())
					((ICompilationUnit) e).delete(false, monitor);
			}else{
				System.out.println(fullName1);
				System.out.println(path);
				throw new Exception();
			}
		}
	
		return newProject;
	}
	
	private void replaceToken(String fullName1, String shortName1, String newText,
			IPackageFragment pack, IProject newProject, IProgressMonitor monitor) throws CoreException{
		IJavaProject javaProject = JavaCore.create(newProject);
		//System.out.println("===============================");
		//System.out.println("former class: "+fullName1);
		String newImport = pack.getElementName()+"."+newText;
		
		SearchForText search = new SearchForText();
		Result result1 = search.searchFor(fullName1,shortName1,newText, javaProject, monitor);
		//System.out.println("找到"+result1.locs.keySet().size()+"个CU中包含formerclass "+fullName1);
		for(ICompilationUnit unit:result1.locs.keySet()){
			ArrayList<Integer[]> offsetsAndLengths = result1.locs.get(unit);
			replaceToken(offsetsAndLengths,unit,newText,monitor);
			if(!unit.getImport(newImport).exists()){
				unit.createImport(newImport, null, monitor);
				unit.save(monitor, true);
			}
		}
		
		newProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		newProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		javaProject = JavaCore.create(newProject);
		
		Result result2 = search.searchFor2(fullName1,shortName1,newText,javaProject, monitor);
		//System.out.println("找到"+result2.locs.keySet().size()+"个CU中包含formerclass "+fullName1);
		for(ICompilationUnit unit:result2.locs.keySet()){
			ArrayList<Integer[]> offsetsAndLengths = result2.locs.get(unit);
			replaceToken(offsetsAndLengths, unit, newImport,monitor);
		}
		//System.out.println("===============================");
	}
	
	private void replaceToken(ArrayList<Integer[]> offsetsAndLengths, ICompilationUnit unit, String newText,IProgressMonitor monitor) throws JavaModelException{
		int temp=0;
		//System.out.println("在unit "+unit.getElementName()+"中有"+offsetsAndLengths.size()+"个"+newText);
		
		offsetsAndLengths.sort(new Comparator<Integer[]>(){
			@Override
			public int compare(Integer[] arg0, Integer[] arg1) {
				return arg0[0]-arg1[0];
			}
		});
		IBuffer buffer = unit.getBuffer();
		for(Integer[] offsetsAndLength:offsetsAndLengths){
			int offset = offsetsAndLength[0];
			//System.out.println("offset: "+offset);
			//System.out.println("temp: "+temp);
			int length = offsetsAndLength[1];
			int begin = offset+temp;
			int end = offset+length+temp;
			//System.out.println(buffer.getChar(begin-1)+"--"+buffer.getChar(end));
			if(isDigitalOrLetter(buffer.getChar(begin-1)) || isDigitalOrLetter(buffer.getChar(end))){
				//System.out.println("判定前后有数字或字母");
				continue;
			}
			buffer.replace(begin, length, newText);
			//System.out.println("已替换");
			temp += newText.length()-length;
			
		}
		buffer.save(null, true);
		buffer.close();
	}
    
    private boolean isDigitalOrLetter(char ch){
		if(ch>=48 && ch<=57){
			return true;
		}
		if(ch>=65 && ch<90){
			return true;
		}
		if(ch>=97 && ch<122){
			return true;
		}
		if(ch==95)
			return true;
		return false;
	}
	
	private boolean hasErrorMarkForUnit(ICompilationUnit unit,IProgressMonitor monitor) throws Exception{
		unit.getJavaProject().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		IResource r = unit.getResource();
		try {
			IMarker[] markers = r.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ZERO);
			for (IMarker marker: markers){
			//	System.out.println("\t"+r.getName()+" has "+marker.getType());
		        Integer severityType = (Integer) marker.getAttribute(IMarker.SEVERITY);
		        if (severityType.intValue() == IMarker.SEVERITY_ERROR){
//					CombineAction.error_count++;
//					if(CombineAction.error_count>CombineAction.k)
//						throw new Exception();
//		        	throw new Exception();
		        	deleteCompilationUnit(unit,monitor);
		        	return true;
		        }
		    }    	
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void deleteCompilationUnit(ICompilationUnit unit, IProgressMonitor monitor) throws JavaModelException{
		if(unit.exists()){
			if(unit.hasUnsavedChanges()){
				System.out.println("unit has unsaved change");
				unit.save(monitor, true);
			}if(unit.isOpen()){
				System.out.println("unit is open");
				unit.close();
			}
			try{
			unit.delete(true, monitor);
			}catch(JavaModelException e){
				unit.delete(true, monitor);
			}
			return;
		}else{
			System.out.println("has been delete");
		}
	}
	
	private void createNewType(String className, ICompilationUnit newUnit, IProgressMonitor monitor){
		try {
			for(int i=0; i<this.combineSize; i++){
				if(CombineAction.isGeneric(mainTypes[i])){
					className = getParameterSignature(mainTypes[i], className);
				}
			}
			String abstractClassCode = getAbstractClassCode(mainTypes);
			String extendsCode = getExtendsCode(mainTypes);
			String implementsCode = getImplementsCode(mainTypes);
			String parentsName ="";
			for(int i=0; i<this.combineSize; i++){
				parentsName+=mainTypes[i].getFullyQualifiedName()
						+" key: "+mainTypes[i].getTypeParameterSignatures().length+"\n";
			}
			String javaDoc = "\n"+"/**"+"\n"+parentsName+"**/";
			String typeCode = javaDoc+"\npublic "+abstractClassCode
					+"class " + className + extendsCode + implementsCode+ "{\n\n}";
			IType mainType = newUnit.createType(typeCode, null, false, monitor);
			
			HashSet<String> allFields = getFields(mainTypes, mainType);
			//System.out.println("allFields size:"+allFields.size());
			ArrayList<String> allConstructors = getConstructors(mainTypes, mainType);
			//System.out.println("allConstructors size:"+allConstructors.size());
			ArrayList<String> allMethods = getMethods(mainTypes, mainType);
			//System.out.println("allMethods size:"+allMethods.size());
			ArrayList<IType> innerTypes = getAllInnerTypes(mainTypes);
			//System.out.println("innerTypes size:"+innerTypes.size());
			
//			Collections.shuffle(allFields);
//			Collections.shuffle(allConstructors);
//			Collections.shuffle(allMethods);
			
			//int k=0;
			for(String field:allFields){
				//System.out.println(k++);
				//System.out.println(field);
				try{
					mainType.createField(field, null, true, monitor);
				}catch(JavaModelException e){
					System.out.println(field);
					//newUnit.delete(true, null);
					return;
				}
			}

			for(String method:allConstructors){
				//System.out.println(method);
				mainType.createMethod(method, null, true, monitor);
			}

			for(String method:allMethods){
				//System.out.println(method);
				mainType.createMethod(method, null, true, monitor);
			}

			for(IType type:innerTypes){
				//System.out.println(type.getSource());
				mainType.createType(type.getSource(), null, true, monitor);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} 
		
	}
	
	private ArrayList<IImportDeclaration> getImports(ICompilationUnit[] units) throws JavaModelException{
		ArrayList<IImportDeclaration> allImports = new ArrayList<IImportDeclaration>();
		for(int i=0; i<this.combineSize; i++){
			IImportDeclaration[] imports1 = units[i].getImports();
			List temp1 = Arrays.asList(imports1);
			allImports.addAll(temp1);
		}
		return allImports;
	}
	
	private String getParameterSignature(IType genericType, String className) throws JavaModelException{
		String[] TPSignature = genericType.getTypeParameterSignatures();
		className += "<";
		for(int k=0; k < TPSignature.length; k++){
			String sig = TPSignature[k].substring(0, TPSignature[k].length()-1);
//			String sig = TPSignature[k];
//			System.out.println(sig);
//			if(Signature.getTypeVariable(TPSignature[k]).equals("T"))
//				sig = "T";
//			else
//				sig = Signature.toString(TPSignature[k]);
			String add = " extends ";
			if(Signature.getTypeParameterBounds(TPSignature[k]).length>0){
				for(String pBounds : Signature.getTypeParameterBounds(TPSignature[k])){
					System.out.println(pBounds);
					add += Signature.toString(pBounds);
				}
			}
			className += sig+add;
			if(k==TPSignature.length-1)
				className += ">";
			else
				className += ", ";
		}
		return className;
	}
	
	private String getAbstractClassCode(IType[] mainTypes){
		boolean isAbstract = false;
		for(int i=0; i<this.combineSize; i++){
			try {
				if((mainTypes[i].getFlags()&Flags.AccAbstract)!=0){
					isAbstract = true;
					break;
				}
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String abstractClassCode = "";
		if(isAbstract)
			abstractClassCode = "abstract ";
		
		return abstractClassCode;
	}
	
	private String getExtendsCode(IType[] mainTypes) throws JavaModelException{
		String extendsCode = "";
		for(int i=0; i<this.combineSize; i++){
			if(mainTypes[i].getSuperclassName()!=null)
				extendsCode = " extends "+ mainTypes[i].getSuperclassName()+" ";
		}
		
		return extendsCode;
	}
	
	private String getImplementsCode(IType[] mainTypes) throws JavaModelException{
		String implementsCode = "";
		HashSet<String> interfaceSignature = new HashSet<String>();
		for(int k=0; k<this.combineSize; k++){
			for(int i=0; i<mainTypes[k].getSuperInterfaceTypeSignatures().length; i++){
				interfaceSignature.add(mainTypes[k].getSuperInterfaceTypeSignatures()[i]);
			}
		}

		if(!interfaceSignature.isEmpty()){
			implementsCode += " implements ";
			Iterator<String> it = interfaceSignature.iterator();
			while(it.hasNext()){
				implementsCode += Signature.toString(it.next())+",";
			}
			implementsCode = implementsCode.substring(0, implementsCode.length()-1);
		}
		
		return implementsCode;
	}
	
	private HashSet<String> getFields(IType[] types, IType newType) throws JavaModelException{
		HashSet<String> allFields = new HashSet<String>();
		int formerClass = 0;
		for(int i=0; i<this.combineSize; i++){
			IField[] fields1 = types[i].getFields();
			for(IField field: fields1){
				String fCode = getRealFieldSource(field, types[i].getElementName(), newType.getElementName());
				//System.out.println(fCode);
				allFields.add(fCode);
				piece.addMembers(field.getElementName(), formerClass);
			}
			formerClass++;
		}

		return allFields;
	}
	
	private ArrayList<String> getConstructors(IType[] types, IType newType) throws JavaModelException{
		ArrayList<String> allConstructors = new ArrayList<String>();
		for(int i=0; i<this.combineSize; i++){
			IMethod[] methods1 = types[i].getMethods();
			for(IMethod method:methods1){
				if(!method.isConstructor())
					continue;
				String mCode = getRealMethodSource(method, types[i].getElementName(), newType.getElementName());
				allConstructors.add(mCode);
				//piece.addMembers(method.getElementName(), 0);
			}
		}

		return allConstructors;
	}
	
	private ArrayList<String> getMethods(IType[] types, IType newType) throws JavaModelException{
		ArrayList<String> allMethods = new ArrayList<String>();
		int formerClass = 0;
		for(int i=0; i<this.combineSize; i++){
			IMethod[] methods1 = types[i].getMethods();
			for(IMethod method:methods1){
				if(method.isConstructor())
					continue;
				String mCode = getRealMethodSource(method, types[i].getElementName(), newType.getElementName());
				allMethods.add(mCode);
				piece.addMembers(method.getElementName(), formerClass);
			}
			formerClass++;
		}
		
		return allMethods;
	}
	
	private String getRealMethodSource(IMethod method, String formerType, String newType) throws JavaModelException{
		String mCode =method.getSource();
		mCode = replaceClassName(mCode, formerType, newType);
		return mCode;
	}
	
	private String getRealFieldSource(IField field, String formerType, String newType) throws JavaModelException{
		String fCode = field.getSource();
		fCode = replaceClassName(fCode, formerType, newType);
		return fCode;
	}
	
	private String replaceClassName(String mCode, String formerType, String newType){
		
		if(mCode.contains(formerType)){
			String returnStr = "";
			String[] splits = mCode.split(formerType);
			
			for(int i=0; i< splits.length-1; i++){
				String split1 = splits[i];
				//System.out.println("split1 = "+split1);
				char leftChar;
				if(split1.isEmpty())
					leftChar = ' ';
				else
					leftChar = split1.charAt(split1.length()-1);	
				String split2 = splits[i+1];
				char rightChar = split2.charAt(0);
				
				if(checkASCII(leftChar) || checkASCII(rightChar)){
					returnStr += split1+formerType;
				}else if(leftChar=='.'){
					returnStr += split1+formerType;
				}else
					returnStr += split1+newType;	
			}
			returnStr += splits[splits.length-1];
			return returnStr;
		}
		return mCode;
	}
	
	private boolean checkASCII(char c){
		if(c>'0' && c<'9')
			return true;
		if(c>'A' && c<'Z')
			return true;
		if(c>'a' && c<'z')
			return true;
		if(c=='$')
			return true;
		
		return false;
	}
	
	private ArrayList<IType> getAllInnerTypes(IType[] mainTypes) throws JavaModelException{
		ArrayList<IType> innerTypes = new ArrayList<IType>();
		for(int i=0; i<this.combineSize; i++){
			for(IType type:mainTypes[i].getTypes()){
				innerTypes.add(type);
			}
		}

		return innerTypes;
	}
	
//	private String replaceBlank(String str) {  
//	    String dest = "";  
//	    if (str != null) {  
//	        Pattern p = Pattern.compile("\\s{4,}");  
//	        Matcher m = p.matcher(str);  
//	        dest = m.replaceAll("\n");  
//	    }  
//	    return dest;  
//	}  
}
