package swr.actions.combine.manuallabel;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.csvreader.CsvReader;
import com.sun.javafx.print.Units;

import swr.actions.aider.ActionsAider;
import swr.actions.combine.CombineAction;
import swr.actions.combine.CombineProject;
import swr.actions.combine.ComparationWithJDeodorant;
import swr.actions.combine.export.ExportDataSetAction;
import swr.actions.combine.search.SearchForText;
import swr.actions.combine.search.SearchForText.Result;

public class CalculateManualLabeledAction implements IWorkbenchWindowActionDelegate {

	@Override
	public void run(IAction arg0) {
		IProject[] projects = ActionsAider.getProjects();
		for(int i=0; i<projects.length; i++){
			try {
				getValidatedFile(projects[i], new NullProgressMonitor());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	private void getValidatedFile(IProject project, IProgressMonitor monitor) throws Exception{
		IFolder folder = project.getFolder("docs/Validated");
		IJavaProject javaProject = JavaCore.create(project);
		
		ArrayList<ICompilationUnit> labeledGodClassUnits = new ArrayList<ICompilationUnit>();
		ArrayList<String> labeledGodClassNames = new ArrayList<String>();
		if(folder.exists()){
			IFile file = folder.getFile("candidate_Large_Class.csv");
			if(file.exists()){
				String charset = file.getCharset();
				Charset cs = Charset.forName(charset);
				CsvReader csvReader = new CsvReader(file.getContents(), cs);
				while(csvReader.readRecord()){
					String csvLine = csvReader.getRawRecord();
					String[] splitedLines = csvLine.split(";");
					String mergedName = splitedLines[1].trim()+"."+splitedLines[0].substring(0, splitedLines[0].lastIndexOf('.')).trim();
					SearchForText search = new SearchForText();
					Result result = search.searchForTypeDeclaration(mergedName, javaProject, monitor);
					if(result.units.size()!=1){
						System.out.println(result.units.size());
						System.out.println(mergedName);
						for(ICompilationUnit unit:result.units){
							System.out.println(unit.getPath());
						}
						throw new Exception();
					}
					labeledGodClassNames.add(mergedName);
					labeledGodClassUnits.add(result.units.get(0));
				}
				csvReader.close();
			}else
				System.out.println("没找到csv文件");
		}else
			System.out.println("没找到Validated文件夹");
		
		ArrayList<ICompilationUnit> units = getAllCU(javaProject);
		calculateJDeodorantResult(labeledGodClassNames,units,project);
		exportDataSet(labeledGodClassUnits,units,project);
	}
	
	private ArrayList<ICompilationUnit> getAllCU(IJavaProject project) throws JavaModelException{
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
		if(mainType.isInterface()||mainType.isEnum())
			return false;
		
		return true;
	}
	
	private void calculateJDeodorantResult(final ArrayList<String> labeledGodClasses,ArrayList<ICompilationUnit> units,
			IProject project) throws Exception{
		System.out.println(units.size());
		ComparationWithJDeodorant compare = new ComparationWithJDeodorant(project);
		final ArrayList<String> jdeodorantGodClasses = compare.getProjectJDeodorantResult(units);
		
		ArrayList<String> temp = (ArrayList<String>) labeledGodClasses.clone();
		System.out.println(jdeodorantGodClasses.size());
		for(String jdeodorantGodClass:jdeodorantGodClasses){
			System.out.println(jdeodorantGodClass);
		}
		System.out.println(labeledGodClasses.size());
		for(String labeledGodClass:labeledGodClasses){
			System.out.println(labeledGodClass);
		}
		System.out.println("========================");
		temp.retainAll(jdeodorantGodClasses);
		int tp = temp.size();
		double precision = (tp*1.0)/jdeodorantGodClasses.size();
		double recall = (tp*1.0)/labeledGodClasses.size();
		double f1_score = (2*precision*recall)/(precision+recall);
		System.out.println(jdeodorantGodClasses.size());
		System.out.println(labeledGodClasses.size());
		System.out.println(tp);
		System.out.println(precision);
		System.out.println(recall);
		System.out.println("f1 score: "+f1_score);
	}

	private void exportDataSet(ArrayList<ICompilationUnit> labeledGodClasses, 
			ArrayList<ICompilationUnit> units,IProject project) throws Exception{
		ExportDataSetAction export = new ExportDataSetAction(CombineAction.dir+"//manual-"+project.getName());
		export.exportFullMN(units);
		units.removeAll(labeledGodClasses);
		export.exportTrainUnits(units, project, 0);
		export.exportTrainUnits(labeledGodClasses, project, 1);
	}
	
	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub

	}

}
