package swr.actions.combine;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import swr.actions.aider.ActionsAider;

public class CombineAction implements IWorkbenchWindowActionDelegate{
	//public static int numOfPieces = 0;
//	int MAX_CU_NUM = 10000;
	public static int error_count = 0;
	public static int k=10;
	public static String dir = "D://卜依凡//学习//论文-检测God Class//数据集//trainset_0707//";
	public void run(IAction action) {
		long begin = System.currentTimeMillis();
		IProject[] projects = ActionsAider.getProjects();
		for(int i=0; i<projects.length; i++){
			CombineProject cp = new CombineProject(projects[i]);
			cp.start(dir);
		}
		System.out.printf("took %.4f seconds", (System.currentTimeMillis()-begin)/1000.0);
	}
	
	void openEndDialog(){
		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
            	MessageBox dialog=new MessageBox(shell,SWT.OK|SWT.ICON_INFORMATION);
 		        dialog.setText("Finish");
 		        dialog.setMessage("Combination Finished!");
 		        dialog.open();
 		        return;
            }
		});
	}

	static boolean isGeneric(IType type) throws JavaModelException{
		if(type.getTypeParameterSignatures().length!=0){
			return true;
		}
		return false;
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
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}
	
}
