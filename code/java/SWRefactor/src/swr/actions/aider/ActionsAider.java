package swr.actions.aider;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

public class ActionsAider {
	public static IType getMainType(ICompilationUnit unit){
		String unitName = unit.getElementName();
		String typeName = unit.getElementName().substring(0, unitName.indexOf('.'));
		IType mainType = unit.getType(typeName);
		return mainType;
	}
	
	@SuppressWarnings("restriction")
	public static IProject getProject() {
		IProject project = null;  
//		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();  
		
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();    
		ISelection selection = selectionService.getSelection();    
		if(selection instanceof IStructuredSelection) {    
			Object element = ((IStructuredSelection)selection).getFirstElement();    
			
			if (element instanceof IResource) {    
				project= ((IResource)element).getProject();    
			} else if (element instanceof PackageFragmentRootContainer) {    
				IJavaProject jProject =     
						((PackageFragmentRootContainer)element).getJavaProject();    
				project = jProject.getProject();    
			} else if (element instanceof IJavaElement) {    
				IJavaProject jProject= ((IJavaElement)element).getJavaProject();    
				project = jProject.getProject();    
			}  
		}     
		
		
		return project; 
	}
	
	public static void printCurrentMemory(String state){
		System.out.println("============="+state+"==============");
//		Runtime runtime = Runtime.getRuntime();
//		int freeMemory = (int) (runtime.freeMemory()/1024/1024);
//		int totalMemory = (int) (runtime.totalMemory()/1024/1024);
		//int maxMemory = (int) (runtime.maxMemory()/1024/1024);
//		System.out.println("free memory: "+freeMemory+"\ntotal memory: "+totalMemory);
		SimpleDateFormat dame = new SimpleDateFormat();
		System.out.println(dame.format(new Date()));
//		System.out.println("=============after gc==============");
//		runtime.gc();
//		runtime.runFinalization();
//		freeMemory = (int) (runtime.freeMemory()/1024/1024);
//		totalMemory = (int) (runtime.totalMemory()/1024/1024);
//		//maxMemory = (int) (runtime.maxMemory()/1024/1024);
//		System.out.println("free memory: "+freeMemory+"\ntotal memory: "+totalMemory);
	}
	
	@SuppressWarnings("restriction")
	public static IProject[] getProjects() {
		IProject[] projects = null;
//		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();  
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.getDescription().setAutoBuilding(false);
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();    
		ISelection selection = selectionService.getSelection();    
		if(selection instanceof IStructuredSelection) {    
			Object[] elements = ((IStructuredSelection)selection).toArray();
			projects = new IProject[elements.length];
			for(int i=0; i<elements.length; i++){
				Object element = elements[i];
				IProject project = null;
				if (element instanceof IResource) {    
					project= ((IResource)element).getProject();    
				} else if (element instanceof PackageFragmentRootContainer) {    
					IJavaProject jProject =     
							((PackageFragmentRootContainer)element).getJavaProject();    
					project = jProject.getProject();    
				} else if (element instanceof IJavaElement) {    
					IJavaProject jProject= ((IJavaElement)element).getJavaProject();    
					project = jProject.getProject();    
				}
				projects[i] = project;
			}

		}     
		
		
		return projects; 
	}
}
