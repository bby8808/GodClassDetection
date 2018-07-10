/*
 * Copyright (c) 2003 Frank Sauer. All rights reserved.
 *
 * Licenced under CPL 1.0 (Common Public License Version 1.0).
 * The licence is available at http://www.eclipse.org/legal/cpl-v10.html.
 *
 *
 * DISCLAIMER OF WARRANTIES AND LIABILITY:
 *
 * THE SOFTWARE IS PROVIDED "AS IS".  THE AUTHOR MAKES  NO REPRESENTATIONS OR WARRANTIES,
 * EITHER EXPRESS OR IMPLIED.  TO THE EXTENT NOT PROHIBITED BY LAW, IN NO EVENT WILL THE
 * AUTHOR  BE LIABLE FOR ANY DAMAGES, INCLUDING WITHOUT LIMITATION, LOST REVENUE,  PROFITS
 * OR DATA, OR FOR SPECIAL, INDIRECT, CONSEQUENTIAL, INCIDENTAL  OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF  LIABILITY, ARISING OUT OF OR RELATED TO
 * ANY FURNISHING, PRACTICING, MODIFYING OR ANY USE OF THE SOFTWARE, EVEN IF THE AUTHOR
 * HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *
 * $id$
 */
package swr.init;

import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class providing access to the metrics framework.
 * 
 * @author Frank Sauer
 */
public class Plugin extends AbstractUIPlugin implements IPropertyChangeListener {

	
	// The shared instance.
	public static Plugin plugin;
	// Resource bundle.
	


	public Plugin() {
		super();
		if (plugin == null) {
			plugin = this;
			
		}
		
	}

	

	


	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop()
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		//Cache.singleton.close();
		super.stop(context);
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start()
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
//		MetricsPlugin mp = new MetricsPlugin(this);
//		mp.installExtensions();

//		Thread thread = new Thread(Detector.GetInstance());
//		thread.start();
		
//        IElementChangedListener eListener = new ElementChangeAction();
//        JavaCore.addElementChangedListener(eListener);
        
//        IResourceChangeListener listener = new ResourceChangeAction();
//        ResourcesPlugin.getWorkspace().addResourceChangeListener(
//           listener, IResourceChangeEvent.PRE_BUILD);
        
       //		
//        IWorkspace workspace = ResourcesPlugin.getWorkspace();  
//        IWorkspaceRoot root = workspace.getRoot(); 
//        for (IProject project : root.getProjects()) {
//        	if(!project.hasNature(Nature.ID)){
//	        	IProjectDescription description = project.getDescription();  
//	        	ProjectUtil.addNature2Project(description, new String[]{Nature.ID});  
//	        	project.setDescription(description, null);  
//        	}
//       	}
        
	}






	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	

}
