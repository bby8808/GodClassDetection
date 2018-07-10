package swr.actions;


import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class PartAction implements IPartListener{

	@Override
	public void partActivated(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
			EditorPart editor = (EditorPart) part;
			//editor.setKeyBindingScopes(null);
			
			
			
//			System.out.println("java editor active");
			IEditorInput input = editor.getEditorInput();  
		    final IDocument doc = ((ITextEditor)editor).getDocumentProvider().getDocument(input);
		    
		    if (doc == null) return;
		    doc.addDocumentListener(new DocumentAction());
		    
		
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partClosed(IWorkbenchPart arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partDeactivated(IWorkbenchPart arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partOpened(IWorkbenchPart arg0) {
		// TODO Auto-generated method stub
		
	}

}
