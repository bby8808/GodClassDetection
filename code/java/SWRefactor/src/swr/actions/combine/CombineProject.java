package swr.actions.combine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import gr.uom.java.ast.SystemObject;
import gr.uom.java.distance.DistanceMatrix;
import gr.uom.java.distance.ExtractClassCandidateRefactoring;
import gr.uom.java.distance.MyClass;
import gr.uom.java.distance.MySystem;
import gr.uom.java.ast.ASTReader;
import gr.uom.java.ast.ClassObject;
import gr.uom.java.ast.CompilationErrorDetectedException;
import swr.actions.aider.ActionsAider;
import swr.actions.combine.export.ExportDataSetAction;

public class CombineProject {
	private IProject project;
//	private int rightCount = 0;
//	private int totalCount = 0;
//	private int tp = 0;
//	private int fp = 0;
//	private int fn = 0;
//	private int tn = 0;
	private int count = 0;
	//private List<CombinationPiece> pieces = new ArrayList<CombinationPiece>();
	public CombineProject(IProject project){
		this.project = project;
	}
	
	public void start(String dir){
		System.out.println("get "+project.getName());
		IJavaProject javaProject = JavaCore.create(project);

		try { 
			ExportDataSetAction exportAction = new ExportDataSetAction(dir+"/"+project.getName());
			List<ICompilationUnit> CUPool = getAllCU(javaProject);
			
			exportAction.exportFullMN(CUPool);
			System.out.println("CUPool size:"+CUPool.size());
			
			ActionsAider.printCurrentMemory("begin all combination");
			ArrayList<Integer> positivesIndeces = combineForTwo(project,CUPool,exportAction,new NullProgressMonitor());
			ArrayList<ICompilationUnit> negativeSamples = getNegatives(CUPool, positivesIndeces);
			//pieces.addAll(combineForThree(exportAction,preCombination,CUPool));
			ActionsAider.printCurrentMemory("end all "+(positivesIndeces.size()/2)+" combination");

			ActionsAider.printCurrentMemory("begin export "+(positivesIndeces.size()/2)+" trainset for "+javaProject.getElementName());
			exportAction.exportTrainUnits(negativeSamples,project,0);
			ActionsAider.printCurrentMemory("negative-"+exportAction.size1+" positive-"+exportAction.size2);
//			collectNegativeCount(project,negativeSamples);
//			double accuracy = (rightCount*1.0)/totalCount;
//			double precision = (tp*1.0)/(tp+fp);
//			double recall = (tp*1.0)/(tp+fn);
//			double f1_score = 2*((precision*recall)/(precision+recall));
//			ActionsAider.printCurrentMemory("negative-"+exportAction.size1+" positive-"+exportAction.size2+" total-"+totalCount+" right-"+rightCount+" accuracy-"+accuracy);
//			ActionsAider.printCurrentMemory("precision-"+precision+" recall-"+recall+" f1 score-"+f1_score);
		}
		catch (CoreException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
		//	DeleteAction delete = new DeleteAction();
			
			try {
				//delete.deleteAllCompbinationPieces(javaProject, pieces);
				ResourcesPlugin.getWorkspace().getRoot().clearHistory(null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("all pieces: "+numOfPieces);
		}
	}
	
	private void collectNegativeCount(IProject project, ArrayList<ICompilationUnit> negativeSamples) throws Exception{
//		ComparationWithJDeodorant compare = new ComparationWithJDeodorant(project);
//		totalCount += negativeSamples.size();
//		rightCount+=compare.getNegativesJDeodorantResult(negativeSamples);
//		tn+=compare.getNegativesJDeodorantResult(negativeSamples);
//		fn+=(negativeSamples.size()-compare.getNegativesJDeodorantResult(negativeSamples));
	}
	
	private List<ICompilationUnit> getAllCU(IJavaProject project) throws JavaModelException{
		List<ICompilationUnit> allUnits = new ArrayList<ICompilationUnit>();
		for(IPackageFragment pack: project.getPackageFragments()){
			for(ICompilationUnit cu:pack.getCompilationUnits()){
				allUnits.add(cu);
			}
		}
		return filteCUPool(allUnits);
	}
	
	public ArrayList<ICompilationUnit> filteCUPool(List<ICompilationUnit> units) throws JavaModelException{
		ArrayList<ICompilationUnit> CUPool = new ArrayList<ICompilationUnit>();

		for(int i=0; i<units.size(); i++)
			if(preparable(units.get(i)))
				CUPool.add(units.get(i));	

		//System.out.println();
		return CUPool;
	}
	
	boolean preparable(ICompilationUnit unit) throws JavaModelException{
	//	System.out.println(unit.getElementName());
		IType mainType = ActionsAider.getMainType(unit);
		if(!mainType.exists()){
		//	System.out.println("unit has no type");
			return false;
		}

//		IField[] fields = mainType.getFields();
//		for(IField field:fields){
//			if((field.getFlags()&Flags.AccFinal)!=0){
//			//	System.out.println("field "+field.getElementName()+" 鏄痜inal灞炴��");
//			//	System.out.println(field.getSource().indexOf('='));
//				if(field.getSource().indexOf('=')==-1){
//				//	System.out.println("娌℃湁鍒濆鍖�");
//					return false;
//				}
////				if(field instanceof SourceField){
////					SourceFieldElementInfo info = (SourceFieldElementInfo) ((SourceField)field).getElementInfo();
////					System.out.println(info);
////				}
//			}
//		}
		
		IMethod[] methods = mainType.getMethods();
		for(IMethod method:methods){
			if(method.isConstructor()){
				if(method.getNumberOfParameters()==0)
					return false;
			}
		}
		
		if(mainType.isInterface()||mainType.isEnum()|| Flags.isAbstract(mainType.getFlags()))
			return false;
		
		if(unit.getAllTypes().length>1)
			return false;
		
		return true;
	}
	
	private ArrayList<ICompilationUnit> getNegatives(List<ICompilationUnit> allUnits, List<Integer> positives){
		ArrayList<ICompilationUnit> negatives = new ArrayList<ICompilationUnit>();
//		for(Integer i:positives){
//			System.out.println(i);
//		}
		for(int i=0; i<allUnits.size(); i++){
			if(!positives.contains(i)){
				negatives.add(allUnits.get(i));
			}
		}
		if(negatives.size()>(positives.size()/2)){
			ArrayList<ICompilationUnit> negativesSamples = new ArrayList<ICompilationUnit>();
			int[] indices = getRandomIndex(0,negatives.size(),positives.size()/2);
			for(int i:indices){
				negativesSamples.add(negatives.get(i));
			}
			return negativesSamples;
		}
		return negatives;
	} 
	
	private int[] getRandomIndex(int seed, int bound, int size){
		Random random = new Random(seed);
		int[] indices = new int[size];
		for(int i=0; i<size; i++){
			indices[i] = random.nextInt(bound);
		}
		return indices;
	}
	
	private ArrayList<Integer> combineForTwo(IProject project, List<ICompilationUnit> CUPool, 
			ExportDataSetAction exportAction,IProgressMonitor monitor) throws Exception{
		ArrayList<Integer> combinedIndex = new ArrayList<Integer>();
		for(int i=0; i<CUPool.size();i++){	
			int j=i+1;
			if(combinedIndex.contains(i))
				continue;
			PreCombination preCombinationForUnitI = new PreCombination(CUPool.get(i), monitor);
			
			boolean combined = false;
			while(!combined && j<CUPool.size()){
				if(combinedIndex.contains(j)){
					j++;
					continue;
				}
				ICompilationUnit[] cps = {CUPool.get(i), CUPool.get(j)};
				int index = 0;
				while(!combined && index<cps.length){
					if(cps[index].getParent() instanceof IPackageFragment){
						IPackageFragment pack = (IPackageFragment) cps[index++].getParent();
						if(preCombinationForUnitI.preCombine(CUPool.get(j),pack, monitor)){

							//System.out.println("combining "+count+"th piece in "+pack.getElementName());
							CombinationPiece piece = combineCompilationUnits(pack, cps, "NewClass"+count,monitor);
							if(piece !=null){
								//pieces.add(piece);
								count++;
								combined=true;
								combinedIndex.add(i);
								combinedIndex.add(j);
								System.out.println(i+"-"+j);
								exportAction.exportPositiveSample(piece,monitor);
								System.out.println("after export");
								collectPositiveCount(piece,monitor);
								System.out.println("after compare with jdeodorant");
								deleteCompilationUnit(piece.getUnit(), monitor);
								System.out.println("after delete piece");
								//return combinedIndex;
							}
						}
					}
				}
				j++;
			}
			ActionsAider.printCurrentMemory("combining for"+i+" piece, already "+(combinedIndex.size()/2));
		}
		return combinedIndex;
	}
	
	private void deleteCompilationUnit(ICompilationUnit unit, IProgressMonitor monitor) throws CoreException{
		if(unit.exists()){
			if(unit.hasUnsavedChanges()){
				System.out.println("unit has unsaved change");
				unit.save(monitor, true);
			}if(unit.isOpen()){
				System.out.println("unit is open");
				unit.close();
			}
			unit.delete(true, monitor);
			
		}else{
			System.out.println("has been delete");
		}
		
		unit.getJavaProject().getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		unit.getJavaProject().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
	}
	
	private void collectPositiveCount(CombinationPiece piece,IProgressMonitor monitor) throws Exception{
//		totalCount++;
//		ComparationWithJDeodorant compare = new ComparationWithJDeodorant(piece.getNewProject());
//		String className = piece.getPack().getElementName()+"."
//				+piece.getElementName().substring(0, piece.getElementName().length()-5);
//		if(compare.getJDeodorantDetectionResult(className)){
//			tp++;
//			rightCount++;
//		}else
//			fp++;
		piece.getNewProject().clearHistory(monitor);
		piece.getNewProject().delete(true, monitor);
	}
	
	public CombinationPiece combineCompilationUnits(IPackageFragment newPack, 
			ICompilationUnit[] units, String className,IProgressMonitor monitor) throws Exception{
		IType[] mainTypes = new IType[units.length];
		for(int i=0; i<units.length; i++){
			IType mainType1 = ActionsAider.getMainType(units[i]);
			mainTypes[i] = mainType1;
		}
		
		Combination combination = new Combination(units, mainTypes);
		CombinationPiece combiPiece = combination.createNewCompilationUnit(project, className, newPack,count,monitor);

		return combiPiece;
	}
	
	
}
