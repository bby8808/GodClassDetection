package swr.actions.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ProjectUtil {
	public static void addNature2Project(IProjectDescription description, String[] natureIds) throws CoreException {  
	    String[] prevNatures = description.getNatureIds();  
	    String[] newNatures = new String[prevNatures.length + natureIds.length];  
	    System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);  
	    for(int i = prevNatures.length;i<newNatures.length;i++){  
	        newNatures[i] = natureIds[i-prevNatures.length];  
	    }  
	    description.setNatureIds(newNatures);  

	    
	}  

	public static void addBuilderToProject(IProjectDescription description,  
	        String[] builderIds, IProgressMonitor monitor) throws CoreException {  
	    ICommand[] buildSpec = description.getBuildSpec();  
	    ICommand[] newBuilders = new ICommand[buildSpec.length  
	            + builderIds.length];  
	    System.arraycopy(buildSpec, 0, newBuilders, 0, buildSpec.length);  
	    for (int i = buildSpec.length; i < newBuilders.length; i++) {  
	        ICommand command = description.newCommand();  
	        command.setBuilderName(builderIds[i - buildSpec.length]);  
	        newBuilders[i] = command;  
	    }  
	    description.setBuildSpec(newBuilders);  
	}  

	public static void removeBuilderFromProject(  
	        IProjectDescription description, String[] builderIds,  
	        IProgressMonitor monitor) {  
	    ICommand[] buildSpec = description.getBuildSpec();  
	    List<ICommand> newBuilders = new ArrayList<ICommand>();  
	    for (ICommand command : buildSpec) {  
	        boolean find = false;  
	        for (String id : builderIds) {  
	            if (command.getBuilderName().equals(id)) {  
	                find = true;  
	                break;  
	            }  
	        }  
	        if (!find) {  
	            newBuilders.add(command);  
	        }  
	    }  
	    description.setBuildSpec(newBuilders.toArray(new ICommand[0]));  
	}  
}
