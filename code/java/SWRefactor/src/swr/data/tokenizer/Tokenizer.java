package swr.data.tokenizer;

public class Tokenizer {

//	public static void main(String[] args){
//		System.out.println(tokenize("void_mainString"));
//	}
	
	public static String tokenize(String str){
		StringBuffer buffer = new StringBuffer();
		char[] chars = str.toCharArray();
		int i = 0;
		while(i < chars.length && !Character.isAlphabetic(chars[i]) && !Character.isDigit(chars[i])){
			i++;
		}
		if(i == chars.length)
			return str;
		buffer.append(chars[i++]);
		for(; i < chars.length; ++i){
			if(!Character.isAlphabetic(chars[i]) && !Character.isDigit(chars[i])){
				if(buffer.charAt(buffer.length() - 1) != ' ' && i < chars.length - 1)
					buffer.append(' ');
				continue;
			}
			if(Character.isUpperCase(chars[i]) && (Character.isLowerCase(chars[i-1]) || (i < chars.length -1 && Character.isLowerCase(chars[i + 1])))){
				if(buffer.charAt(buffer.length() - 1) != ' ')
					buffer.append(' ');
			}
			if(Character.isDigit(chars[i]) && !Character.isDigit(chars[i-1])){
				if(buffer.charAt(buffer.length() - 1) != ' ')
					buffer.append(' ');
			}
			buffer.append(chars[i]);
			if(Character.isDigit(chars[i]) && i < chars.length - 1 && !Character.isDigit(chars[i + 1])){
				if(buffer.charAt(buffer.length() - 1) != ' ')
					buffer.append(' ');
			}
		}
		return buffer.toString();
	}
}
