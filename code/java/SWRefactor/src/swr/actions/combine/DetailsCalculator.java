package swr.actions.combine;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import swr.actions.aider.ActionsAider;

public class DetailsCalculator implements IWorkbenchWindowActionDelegate{

	@Override
	public void run(IAction arg0) {
		// TODO Auto-generated method stub
		IProject project = ActionsAider.getProject();
		IJavaProject javaProject = JavaCore.create(project);
		int NOC = 0;
		int NOM = 0;
		try {
			for(IPackageFragment pack: javaProject.getPackageFragments()){
				NOC+=pack.getCompilationUnits().length;
				for(ICompilationUnit unit:pack.getCompilationUnits()){
					//NOC+=unit.getAllTypes().length;
					for(IType type:unit.getAllTypes()){
						NOM+=type.getMethods().length;
					}
				}
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("NOC="+NOC);
		System.out.println("NOM="+NOM);
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
