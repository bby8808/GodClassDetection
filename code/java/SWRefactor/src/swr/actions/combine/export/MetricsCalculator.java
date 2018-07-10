package swr.actions.combine.export;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Cache;
import net.sourceforge.metrics.core.sources.Dispatcher;
import swr.actions.aider.ActionsAider;
import swr.actions.combine.CombinationPiece;

public class MetricsCalculator {
 
	//ATFD,DCC,DIT,TCC,LCOM,CAM,WMC,TLOC,NOPA,NOAM,NOA,NOM
	public double[] getMetrics(CombinationPiece piece, IProgressMonitor monitor) throws Exception{
		IJavaProject newJavaProject = JavaCore.create(piece.getNewProject());
		IPackageFragment pack = getPackageInNewProject(newJavaProject,piece);
		ICompilationUnit unit0 = pack.getCompilationUnit(piece.getElementName());
		
		double[] val = getMetrics(unit0);
		//Cache.singleton.clear();
		return val;
	}
	
	//ATFD,DCC,DIT,TCC,LCOM,CAM,WMC,TLOC,NOPA,NOAM,NOA,NOM
	public double[] getMetrics(ICompilationUnit unit) throws Exception{
		IType type = ActionsAider.getMainType(unit);
		System.out.println(type.getPath());
		AbstractMetricSource metric = Dispatcher.calculateAbstractMetricSource(unit);
		double[] metrics = new double[12];
		String[] m_names = {"ATFD","DCC","DIT","TCC","LCOM","CAM","WMC","TLOC","NOPA",
				"NOAM","NOA","NOM"};
		for (int i=0; i<m_names.length; i++) {
			Metric mc = metric.getValue(m_names[i]);
			if (mc != null) {
				metrics[i] = mc.doubleValue();
			}
			else{
				System.out.println(m_names[i]+" is null");
				throw new Exception();
			}
		}
//		for(M_Class m:val.keySet()){
//			System.out.println(m.name()+": "+val.get(m).doubleValue());
//		}
		
		return metrics;
	}
	
	private IPackageFragment getPackageInNewProject(IJavaProject javaProject, 
			CombinationPiece piece) throws JavaModelException{
		return javaProject.findPackageFragment(
				javaProject.getPath().append(piece.getPack().getPath().removeFirstSegments(1)));
	}
	
	public enum M_Class {
		//NUM_PUB_FIELDS, NUM_PUB_ACCESS_METHODS, WMC, DEPTH_OF_INHERITANCE_TREE, ACCESS_TO_FOREIGN_DATA, TCC, NUM_PUB_METHODS
		ATFD,DCC,DIT,TCC,LCOM,CAM,WMC,TLOC,NOPA,NOAM,NOA,NOM
	}
}
