package swr.actions.combine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import gr.uom.java.ast.ASTReader;
import gr.uom.java.ast.ClassObject;
import gr.uom.java.ast.CompilationErrorDetectedException;
import gr.uom.java.ast.SystemObject;
import gr.uom.java.ast.util.math.Cluster;
import gr.uom.java.ast.util.math.Clustering;
import gr.uom.java.distance.DistanceMatrix;
import gr.uom.java.distance.Entity;
import gr.uom.java.distance.ExtractClassCandidateRefactoring;
import gr.uom.java.distance.MyClass;
import gr.uom.java.distance.MySystem;
import swr.actions.aider.ActionsAider;

public class ComparationWithJDeodorant {
	IJavaProject javaProject;
	SystemObject so;
	MySystem system;
	DistanceMatrix distanceMatrix;

	public ComparationWithJDeodorant(IProject project){
		IJavaProject javaProject = JavaCore.create(project);
		this.javaProject = javaProject;
		this.so = buildSystemObject();
		this.system = new MySystem(so, false);
		this.distanceMatrix= new DistanceMatrix(system);
		ActionsAider.printCurrentMemory("build the old system");
	}
	
	private SystemObject buildSystemObject(){
		try {
			if(ASTReader.getSystemObject() != null && javaProject.equals(ASTReader.getExaminedProject())) {
				System.out.println("way1");
				new ASTReader(javaProject, ASTReader.getSystemObject(), null);
			}
			else {
				System.out.println("way2");
				new ASTReader(javaProject, null);
			}
		}
		catch(CompilationErrorDetectedException e) {
			e.printStackTrace();
		}
		SystemObject so = ASTReader.getSystemObject();

		return so;
	}
	
	public boolean getJDeodorantDetectionResult(String className) throws Exception{
		MyClass sourceClass = system.getClass(className);
		if(sourceClass==null){
			System.out.println(className);
			throw new Exception();
		}
		return hasCandidate(sourceClass);
	}
	
	private boolean hasCandidate(MyClass sourceClass){
		if (!sourceClass.getMethodList().isEmpty() && !sourceClass.getAttributeList().isEmpty()) {
			double[][] distances = distanceMatrix.getJaccardDistanceMatrix(sourceClass);
			Clustering clustering = Clustering.getInstance(0, distances);
			ArrayList<Entity> entities = new ArrayList<Entity>();
			entities.addAll(sourceClass.getAttributeList());
			entities.addAll(sourceClass.getMethodList());
			HashSet<Cluster> clusters = clustering.clustering(entities);
			for (Cluster cluster : clusters) {
				ExtractClassCandidateRefactoring candidate = new ExtractClassCandidateRefactoring(system, sourceClass, cluster.getEntities());
				if (candidate.isApplicable()) {
					int sourceClassDependencies = candidate.getDistinctSourceDependencies();
					int extractedClassDependencies = candidate.getDistinctTargetDependencies();
					if(sourceClassDependencies <= distanceMatrix.maximumNumberOfSourceClassMembersAccessedByExtractClassCandidate &&
							sourceClassDependencies < extractedClassDependencies) {
						return true;
					}
				}
			}
			// Clustering End
		}
		return false;
	}
	
	public ArrayList<String> getProjectJDeodorantResult(List<ICompilationUnit> units) throws Exception{
		ArrayList<String> jdeodorantGodClasses = new ArrayList<String>();
		for(ICompilationUnit unit: units){
			String name;
			if(unit.getParent().getElementName()!="")
				name = unit.getParent().getElementName()+"."
					+unit.getElementName().substring(0, unit.getElementName().length()-5);
			else
				name = unit.getElementName().substring(0, unit.getElementName().length()-5);
			if(getJDeodorantDetectionResult(name)){
				jdeodorantGodClasses.add(name);
			}
		}
		return jdeodorantGodClasses;
	}
	
	public int getNegativesJDeodorantResult(ArrayList<ICompilationUnit> negativeSamples) throws Exception{
		int rightCount = 0;
		for(ICompilationUnit sample: negativeSamples){
			String name = sample.getParent().getElementName()+"."
					+sample.getElementName().substring(0, sample.getElementName().length()-5);
			if(!getJDeodorantDetectionResult(name)){
				rightCount++;
			}
		}
		return rightCount;
	}
}
