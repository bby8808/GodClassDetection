package swr.actions.combine.delete;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import swr.actions.combine.CombinationPiece;

public class DeleteAction{
	
	public void deleteAllCompbinationPieces(IJavaProject project, List<CombinationPiece> pieces) throws JavaModelException{
		System.out.println();
		System.out.println("========================");
		System.out.println("clear all "+pieces.size()+" new classes in project "+project.getElementName()+"!");
		System.out.println("========================");
		System.out.println();
		for(CombinationPiece p : pieces){
			ICompilationUnit cu = p.getPack().getCompilationUnit(p.getElementName());
			cu.delete(true, null);
		}
		pieces.clear();
		//combinations.clear();

	}
	
	void openEndDialog(){
		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
            	MessageBox dialog=new MessageBox(shell,SWT.OK|SWT.ICON_INFORMATION);
 		        dialog.setText("Finish");
 		        dialog.setMessage("Clear Finished!");
 		        dialog.open();
 		        return;
            }
		});
	}
}
