package swr.actions.combine.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;


public class SearchForText {

	public class Result{
		public Map<ICompilationUnit, ArrayList<Integer[]>> locs = new HashMap<ICompilationUnit, ArrayList<Integer[]>>();
		public boolean flags = false;
		public ArrayList<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
		
		public void setLoc(ICompilationUnit unit, int offset, int length){
			if(locs.containsKey(unit)){
				Integer[] loc = {offset,length};
				locs.get(unit).add(loc);
			}else{
				ArrayList<Integer[]> offsets = new ArrayList<Integer[]>();
				Integer[] loc = {offset,length};
				offsets.add(loc);
				locs.put(unit, offsets);
			}
		}
	}
    
    public Result searchFor(final String patternString, final String name1,final String name2,IJavaProject project,IProgressMonitor monitor) throws CoreException{
    	int indexOfDot = patternString.lastIndexOf('.');
    	final String packForPattern;
    	if(indexOfDot>=0)
    		packForPattern = patternString.substring(0, indexOfDot);
    	else
    		packForPattern = "";
    	SearchPattern pattern = SearchPattern.createPattern(patternString,
				IJavaSearchConstants.TYPE, IJavaSearchConstants.ALL_OCCURRENCES,
				SearchPattern.R_PATTERN_MATCH);
		SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
		IJavaSearchScope scope= SearchEngine.createJavaSearchScope(new IJavaElement[]{project}, IJavaSearchScope.SOURCES);
		final Result result = new Result();
		SearchRequestor requestor = new SearchRequestor(){
			@Override
	    	public void acceptSearchMatch(SearchMatch match) throws CoreException {
				//System.out.println(match.getResource().getName());
	    		Object element = match.getElement();
	    		if(element instanceof IMember){
	    			ICompilationUnit unit = ((IMember) element).getCompilationUnit();
	    			String elementName = unit.getElementName().subSequence(0, unit.getElementName().length()-5).toString();
	    			if(!(name1.equals(elementName) && unit.getParent().getElementName().equals(packForPattern))){
	    			//	System.out.println(element.toString());
	    				result.setLoc(((IMember) element).getCompilationUnit(),match.getOffset(),match.getLength());
	    			}
	    		}
	    	}
		};
		new SearchEngine().search(pattern, participants, scope,	requestor, monitor);
		
		return result;
    }
    
    public Result searchForTypeDeclaration(final String patternString,IJavaProject project, IProgressMonitor monitor) throws CoreException{
    	SearchPattern pattern = SearchPattern.createPattern(patternString,
				IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS,
				SearchPattern.R_PATTERN_MATCH);
		SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
		IJavaSearchScope scope= SearchEngine.createJavaSearchScope(new IJavaElement[]{project}, IJavaSearchScope.SOURCES);
		final Result result = new Result();
		SearchRequestor requestor = new SearchRequestor(){
			@Override
	    	public void acceptSearchMatch(SearchMatch match) throws CoreException {
				//System.out.println(match.getResource().getName());
	    		Object element = match.getElement();
	    		if(element instanceof IMember){
	    			ICompilationUnit unit = ((IMember) element).getCompilationUnit();
	    			result.units.add(unit);
	    		}
	    	}
		};
		new SearchEngine().search(pattern, participants, scope,	requestor, monitor);
		
		return result;
    }
    
    @SuppressWarnings({ "static-access" })
	public Result searchFor2(final String patternString, final String name1,final String name2,
			final IJavaProject project,IProgressMonitor monitor) throws CoreException{
    	//System.out.println(patternString);
    	int indexOfDot = patternString.lastIndexOf('.');
    	final String packForPattern;
    	if(indexOfDot>=0)
    		packForPattern = patternString.substring(0, indexOfDot);
    	else
    		packForPattern = "";
    	System.out.println(packForPattern);
    	//System.out.println("name1="+name1+"--name2="+name2);
    	TextSearchEngine engine2 = TextSearchEngine.create();
    	Pattern pattern = engine2.createPattern(patternString, true, false);
		TextSearchScope scope= TextSearchScope.newSearchScope(project.getProject().members(), engine2.createPattern("*.java", false, false), false);
		final Result result = new Result();
		TextSearchRequestor requestor = new TextSearchRequestor(){
			@Override
			public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {
				IFile file = matchAccess.getFile();
				IJavaElement element = project.findElement(file.getFullPath().makeRelativeTo(project.getPackageFragmentRoots()[0].getPath()));
				//System.out.println(file.getFullPath().makeRelativeTo(project.getPackageFragmentRoots()[0].getPath()));
				//System.out.println("1. "+element.getElementName());
				//System.out.println(element.getClass());
				if(element instanceof ICompilationUnit){
					//System.out.println("2. "+element.getElementName());
					String elementName = element.getElementName().subSequence(0, element.getElementName().length()-5).toString();
					
					//System.out.println("elementName="+elementName);
					if(!(name1.equals(elementName) && element.getParent().getElementName().equals(packForPattern)))
						result.setLoc((ICompilationUnit) element, matchAccess.getMatchOffset(),matchAccess.getMatchLength());
				}
				
				return true;
			}
		};
		try{
			engine2.search(scope, requestor, pattern, monitor);
		}catch(OperationCanceledException e){
			System.out.println("OperationCanceledException when search "+patternString);
			try{
				engine2.search(scope, requestor, pattern, monitor);
			}catch(OperationCanceledException e1){
				System.out.println("OperationCanceledException when search "+patternString);
			}
			
    
		}
		return result;
    }
    
    public boolean SearchForDefaultConstructor(final String constructorFullString,
    		IJavaProject project,IProgressMonitor monitor){
    	SearchPattern pattern = SearchPattern.createPattern(constructorFullString,
				IJavaSearchConstants.CONSTRUCTOR, IJavaSearchConstants.ALL_OCCURRENCES,
				SearchPattern.R_CASE_SENSITIVE);
		SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
		IJavaSearchScope scope= SearchEngine.createJavaSearchScope(new IJavaElement[]{project}, IJavaSearchScope.SOURCES);
		final Result result = new Result();
		SearchRequestor requestor = new SearchRequestor(){
			@Override
	    	public void acceptSearchMatch(SearchMatch match) throws CoreException {
				//System.out.println(match.getResource().getName());
	    		Object element = match.getElement();
	    		if(element instanceof IMember){
	    			//System.out.println("需要无参构造函数");
	    			result.flags = true;
	    		}
	    	}
		};
		
		try {
			new SearchEngine().search(pattern, participants, scope,	requestor, monitor);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result.flags;
    }
	
}
