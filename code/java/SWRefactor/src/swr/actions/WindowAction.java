package swr.actions;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;


public class WindowAction implements IWindowListener{

	@Override
	public void windowActivated(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
//		System.out.println("windows active");
		//add part action listener
	
		window.getPartService().addPartListener(new PartAction());
		System.out.print("!!!!");
	}

	@Override
	public void windowClosed(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
//		System.out.println("window opened");
		
	}

}
