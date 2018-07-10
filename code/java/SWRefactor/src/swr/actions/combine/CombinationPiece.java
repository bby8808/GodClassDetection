package swr.actions.combine;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;

public class CombinationPiece {
	private ICompilationUnit unit;
	private IProject newProject;
	private String name;
	private String[] formerClasses;
	private int combineSize;
	private int length;
	private ArrayList<CombinedMembers> members = new ArrayList<CombinedMembers>();
	private IPackageFragment pack;
	
	public CombinationPiece(IPackageFragment pack, int combineSize){
		this.pack = pack;
		this.setCombineSize(combineSize);
		this.formerClasses = new String[combineSize];
	}

	public void addMembers(String memberName, int formerClass){
		CombinedMembers member = new CombinedMembers(memberName, formerClass);
		members.add(member);
	}
	
	public ArrayList<CombinedMembers> getMembers(){
		return members;
	}

	public IPackageFragment getPack() {
		return pack;
	}

	public void setPack(IPackageFragment pack) {
		this.pack = pack;
	}

	public ICompilationUnit getUnit() {
		return unit;
	}

	public void setUnit(ICompilationUnit unit) {
		this.unit = unit;
	}
	
	public String getElementName(){
		return name;
	}
	
	public void setElementName(String name){
		this.name = name;
	}

	public String[] getFormerClasses() {
		return formerClasses;
	}

	public void setFormerClass(String formerClass) {
		formerClasses[length++] = formerClass.substring(0, formerClass.length()-5);
	}

	public int getCombineSize() {
		return combineSize;
	}

	public void setCombineSize(int combineSize) {
		this.combineSize = combineSize;
	}

	public IProject getNewProject() {
		return newProject;
	}

	public void setNewProject(IProject newProject) {
		this.newProject = newProject;
	}
	
	
}
