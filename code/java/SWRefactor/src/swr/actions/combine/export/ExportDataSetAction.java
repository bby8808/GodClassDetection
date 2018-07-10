package swr.actions.combine.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import net.sourceforge.metrics.core.sources.Cache;
import swr.actions.aider.ActionsAider;
import swr.actions.combine.CombinationPiece;
import swr.actions.combine.CombinedMembers;
import swr.actions.combine.export.MetricsCalculator.M_Class;

public class ExportDataSetAction{
	private String base;
	private String trainDir = "//train";
	private String testDir = "//test";
	private String mnFile = "//mn_train.txt";
	private String lbFile = "//lb_train.txt";
	private String jcFile = "//jc_train.txt";
	private String mtFile = "//mt_train.txt";
	public int size1;
	public int size2;
	//int batchSize = 50000;
	public ExportDataSetAction(String base){
		this.base = base;
		size1 = 0;
		size2 = 0;
	}
	
	
	
//	public double[][] buildJaccardMatrix(CombinationPiece piece) throws Exception{
//		String[] units = piece.getFormerClasses();
//		int size = piece.getCombineSize();
//		MyClass[] classes = new MyClass[size];
//		for(int i=0; i<size; i++){
//			MyClass class1 = system.getClass(units[i]);
//			if(class1==null){
//				System.out.println(units[i]);
//				throw new Exception();
//			}
//			classes[i]=class1;
//		}
//		double[][] jaccard = distanceMatrix.getJaccardDistanceMatrix(classes,false);
//		
//		if(jaccard==null)
//			throw new Exception();
//		if(jaccard.length!=piece.getMembers().size()){
//			System.out.println(piece.getElementName());
//			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//			for(int i=0; i<piece.getMembers().size(); i++){
//				System.out.println(piece.getMembers().get(i));
//			}
//			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//			distanceMatrix.getJaccardDistanceMatrix(classes, true);
//			throw new Exception();
//		}
//		return jaccard;
//	}
	
//	private double[][] transformMatrix(double[][] jaccard, int i){
//		double[][] newJaccard = new double[jaccard.length][jaccard.length];
//		double[] temp1 = jaccard[i];
//		
//		for(int k=jaccard.length-1; k>i; k--){
//			newJaccard[k] = jaccard[k];
//		}
//		for(int k=i; k>0; k--){
//			newJaccard[k] = jaccard[k-1];
//		}
//		newJaccard[0] = temp1;
//
//		double[] temp3 = new double[jaccard.length];
//		for(int k=0; k<jaccard.length; k++){
//			temp3[k] = newJaccard[k][i];
//		}
//		for(int k=i;k>0;k--){
//			for(int m=0; m<jaccard.length; m++){
//				newJaccard[m][k] = newJaccard[m][k-1];
//			}
//		}		
//
//		for(int k=0; k<jaccard.length; k++){
//			newJaccard[k][0]=temp3[k];
//		}
//		return newJaccard;
//	}
//	private double[][] transformMatrix(final double[][] jaccard, int i, int j) throws Exception{
//		//System.out.println("jaccard length:"+jaccard.length);
//
//		double[][] newJaccard = new double[jaccard.length][jaccard.length];
//		double[] temp1 = jaccard[i].clone();
//		double[] temp2 = jaccard[j].clone();
//
//		for(int k=jaccard.length-1; k>j; k--){
//			newJaccard[k] = jaccard[k].clone();
//		}
//		for(int k=j; k>i; k--){
//			newJaccard[k] = jaccard[k-1].clone();
//		}
//		for(int k=i+1; k>1; k--){
//			newJaccard[k] = jaccard[k-2].clone();
//		}
//		newJaccard[0] = temp1;
//		newJaccard[1] = temp2;
////		System.out.println("只换了行的newJaccard：");
////		for(int m=0; m<newJaccard.length; m++){
////			for(int n=0; n<newJaccard[m].length; n++){
////				System.out.print(newJaccard[m][n]+" ");
////			}
////			System.out.println();
////		}
////		System.out.println("===============================");
//		double[] temp3 = new double[newJaccard.length];
//		double[] temp4 = new double[newJaccard.length];
//		for(int k=0; k<newJaccard.length; k++){
//			temp3[k] = newJaccard[k][i];
//			temp4[k] = newJaccard[k][j];
//		}
//		for(int k=j;k>i;k--){
//			for(int m=0; m<newJaccard.length; m++){
//				newJaccard[m][k] = newJaccard[m][k-1];
//			}
//		}		
//		for(int k=i+1; k>1; k--){
//			for(int m=0; m<newJaccard.length; m++){
//				newJaccard[m][k] = newJaccard[m][k-2];
//			}
//		}
//		for(int k=0; k<newJaccard.length; k++){
//			newJaccard[k][0]=temp3[k];
//			newJaccard[k][1]=temp4[k];
//		}
////		System.out.println("newJaccard:");
////		for(int m=0; m<newJaccard.length; m++){
////			for(int n=0; n<newJaccard[m].length; n++){
////				System.out.print(newJaccard[m][n]+" ");
////			}
////			System.out.println();
////		}
////		System.out.println("===============================");
////		for(int m=0; m<newJaccard.length; m++){
////			if(newJaccard[m][m]!=0){
////				System.out.println("i="+i);
////				System.out.println("j="+j);
////				throw new Exception();
////			}
////		}
//		double[][] result = new double[2][newJaccard.length];
//		result[0] = newJaccard[0];
//		result[1][0] = newJaccard[1][1];
//		result[1][1] = newJaccard[1][0];
//		for(int k=2; k<newJaccard.length; k++)
//			result[1][k] = newJaccard[1][k];
//		return result;
//	}
//	
//	private void writerMatrix(double[][] jaccard1,BufferedWriter writer) throws IOException{
//		//writer.write(size+": ");
//		for(int j=0; j<jaccard1[0].length; j++){
//			writer.write(String.valueOf(jaccard1[0][j])+" ");
//		}
//		writer.write(";");
//		for(int j=0; j<jaccard1[1].length; j++){
//			writer.write(String.valueOf(jaccard1[1][j])+" ");
//		}
//		writer.newLine();
//		writer.flush();
//	}
//	
//	private void writeln(CombinationPiece piece, BufferedWriter writer1, BufferedWriter writer2) throws IOException{
//		ArrayList<CombinedMembers> members = piece.getMembers();
//
//		for(int i=0; i<members.size(); i++){
//			for(int j=i+1; j<members.size(); j++){
//				CombinedMembers cmember1 = members.get(i);
//				CombinedMembers cmember2 = members.get(j);
//				String token1 = tokenize(cmember1.getMemberName());
//				String token2 = tokenize(cmember2.getMemberName());
//				writer1.write(token1+".\b");
//				writer1.write(token2+".\n");
//				if(cmember1.getFormerClass() != cmember2.getFormerClass())
//					writer2.write("0"+"\n");
//				else
//					writer2.write("1"+"\n");
//			}
//			
//		}
//	}
//	
	public String tokenize(String line){
        String[] splitString = line.split("((?<=\\.)|(?=\\.))| |((?<=\\{)|(?=\\{))|((?<=\\})|(?=\\}))|((?<=\\()|(?=\\())|((?<=\\))|(?=\\)))|((?<=\\[)|(?=\\[))|((?<=\\])|(?=\\]))|((?<=\\;)|(?=\\;))|((?<=\\,)|(?=\\,))|((?<=\\>)|(?=\\>))|((?<=\\<)|(?=\\<))");
        String rline = "";
		for(String token : splitString){
			token = token.trim();
			if (token != null && !(token.equals(""))){
				token = token.replaceAll("\t", "");
				token = token.replaceAll(" ", "");
				token = token.replaceAll("/", " ");
				token = token.replaceAll("\n", " ");
				boolean isWord=token.matches("[a-zA-Z0-9_$]+");
				boolean isCapitalWord = token.matches("[A-Z_$]+")||token.matches("[A-Z]+")||token.matches("[A-Z_]+")||token.matches("[A-Z$]+");
				if(isWord)
					if(!isCapitalWord){
						token = token.replaceAll("[A-Z0-9]", " $0");
						token = token.replaceAll("[_$]", " $0 ");
						token = token.toLowerCase();
						rline += token + " ";
					}
					else{
						token = token.toLowerCase();
						token = token.replaceAll("[0-9_$]", " $0 ");
						rline += token + " ";
					}
			}
			
		}
		rline = rline.trim();
		rline = rline.replaceAll("[_$]", "");
		rline = rline.replaceAll("   ", " ");
		rline = rline.replaceAll("  ", " ");
		return rline;
	}
	
	private BufferedWriter getWriter(String filePath) throws IOException{
		File file = new File(filePath);
		if(!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		if(!file.exists())
			file.createNewFile();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		return writer;
	}

	public void exportTrainUnits(ArrayList<ICompilationUnit> negativeSamples,IProject project,int label) throws Exception{
		BufferedWriter writer1 = getWriter(base+mnFile);
		BufferedWriter writer2 = getWriter(base+lbFile);
		BufferedWriter writer3 = getWriter(base+mtFile);
		
//		for(CombinationPiece piece:positiveSamples){
//			for(String clazz: piece.getFormerClasses()){
//				System.out.println(clazz);
//			}
//		}
		
		exportNegativeSamples(negativeSamples,label,writer1,writer2,writer3);
//		exportPositiveSamples(positiveSamples,project,writer1,writer2,writer3);

		writer1.close();
		writer2.close();
		writer3.close();		
	}
	
	//ATFD,DCC,DIT,TCC,LCOM,CAM,WMC,TLOC,NOPA,NOAM,NOA,NOM
	private void exportNegativeSamples(ArrayList<ICompilationUnit> negativeSamples, int label,
			BufferedWriter writer1, BufferedWriter writer2, BufferedWriter writer3) throws Exception{

		for(ICompilationUnit sample:negativeSamples){
			IType type = ActionsAider.getMainType(sample);
			for(IField field:type.getFields()){
				String name = field.getElementName();
				String tok = tokenize(name);
				writer1.write(tok+".");
			}
			for(IMethod method:type.getMethods()){
				String name = method.getElementName();
				String tok = tokenize(name);
				writer1.write(tok+".");
			}
			writer1.newLine();
			writer1.flush();
			
			MetricsCalculator calculator = new MetricsCalculator();
			double[] metricsMap = calculator.getMetrics(sample);
			exportMetrics(label,metricsMap,writer2,writer3);
			size1++;
		}
		//Cache.singleton.clear();
	}
	
	public void exportPositiveSample(CombinationPiece sample,IProgressMonitor monitor) throws Exception{
		BufferedWriter writer1 = getWriter(base+mnFile);
		BufferedWriter writer2 = getWriter(base+lbFile);
		BufferedWriter writer3 = getWriter(base+mtFile);
		
		exportPositiveSample(sample, writer1, writer2, writer3, monitor);
		
		writer1.close();
		writer2.close();
		writer3.close();	
	}
	
	private void exportPositiveSample(CombinationPiece sample,
			BufferedWriter writer1, BufferedWriter writer2, BufferedWriter writer3,IProgressMonitor monitor) throws Exception{
		ArrayList<CombinedMembers> members = sample.getMembers();
		for(CombinedMembers member: members){
			String name = member.getMemberName();
			String tok = tokenize(name);
			writer1.write(tok+".");
		}
		writer1.newLine();
		writer1.flush();
		
		MetricsCalculator calculator = new MetricsCalculator();
		double[] metricsMap = calculator.getMetrics(sample,monitor);
		exportMetrics(1,metricsMap,writer2,writer3);
		size2++;
	}
	
	//ATFD,DCC,DIT,TCC,LCOM,CAM,WMC,TLOC,NOPA,NOAM,NOA,NOM
	private void exportMetrics(int label,double[] metricsMap,
			BufferedWriter writer2, BufferedWriter writer3) throws Exception{
		writer2.write(String.valueOf(label));
//		double[] metrics = {metricsMap.get("ATFD"),metricsMap.get("DCC"),metricsMap.get("DIT"),
//                metricsMap.get("TCC"),metricsMap.get("LCOM"),metricsMap.get("CAM"),
//                metricsMap.get("WMC"),metricsMap.get("TLOC"),metricsMap.get("NOPA"),
//                metricsMap.get("NOAM"),metricsMap.get("NOA"),metricsMap.get("NOM")};
		String[] m_names = {"ATFD","DCC","DIT","TCC","LCOM","CAM","WMC","TLOC","NOPA",
				"NOAM","NOA","NOM"};
		for(int i=0; i<metricsMap.length; i++){
			System.out.println(m_names[i]+": "+metricsMap[i]);
			writer3.write(String.valueOf(metricsMap[i])+" ");
		}

		writer2.newLine();
		writer3.newLine();
		
		writer2.flush();
		writer3.flush();
	}
	
//	public void exportTestUnits(List<CombinationPiece> pieces,IProject project) throws Exception{
//		String dir = base+testDir;
//		for(int i=0; i<pieces.size(); i++){
//			String test_dir = dir+"//"+pieces.get(i).getElementName();
//			BufferedWriter writer1 = getWriter(test_dir+mnFile);
//			BufferedWriter writer2 = getWriter(test_dir+lbFile);
//			BufferedWriter writer3 = getWriter(test_dir+jcFile);
//			exportDataSetForPiece(writer1,writer2,writer3,pieces.get(i));
//			writer1.close();
//			writer2.close();
//			writer3.close();
//			
//			ActionsAider.printCurrentMemory("begin export jdeodorant: "+i+"/"+pieces.size());
//			ImportProject buildNewProject = new ImportProject();
//			buildNewProject.checkJDeodorantResult(test_dir, project, pieces, i);
//			ActionsAider.printCurrentMemory("end export jdeodorant: "+i+"/"+pieces.size());
//		}
//	}

	public void exportFullMN(List<ICompilationUnit> units) throws IOException, JavaModelException{
		String mnFullFile = base+"//full_mn//mn_full.txt";
		BufferedWriter writer = getWriter(mnFullFile);
		for(ICompilationUnit unit:units){
			IType mainType = ActionsAider.getMainType(unit);
			for(IField field:mainType.getFields()){
				String name = field.getElementName();
				String token = tokenize(name);
				writer.write(token+"\n");
			}
			for(IMethod method:mainType.getMethods()){
				String name = method.getElementName();
				String token = tokenize(name);
				writer.write(token+"\n");
			}
		}
		writer.flush();
		writer.close();
	}
	
//	private void exportDataSetForPiece(BufferedWriter writer1,
//			BufferedWriter writer2,BufferedWriter writer3,
//			CombinationPiece piece) throws Exception{
//		writeln(piece, writer1, writer2);
//		final double[][] jaccard = buildJaccardMatrix(piece);
//		for(int k=0; k<jaccard.length; k++){
//			for(int j=k+1; j<jaccard.length; j++){
//				double[][] newJaccard1 = transformMatrix(jaccard,k,j);
//				writerMatrix(newJaccard1,writer3);
//			}
//		}
//		writer1.flush();
//		writer2.flush();
//		writer3.flush();
//	}

}
