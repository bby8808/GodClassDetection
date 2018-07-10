package swr.actions.combine;

public class CombinedMembers {
	private String memberName;
	private int formerClass;
	
	public CombinedMembers(String memberName, int formerClass){
		this.setMemberName(memberName);
		this.setFormerClass(formerClass);
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public int getFormerClass() {
		return formerClass;
	}

	public void setFormerClass(int formerClass) {
		this.formerClass = formerClass;
	}
}
