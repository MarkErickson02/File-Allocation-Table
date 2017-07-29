import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/** @author Mark Erickson
 * CS431-P4
 * 
 */
public class FileSystem {
	
	private int[] fileAllocationTable;
	private HashMap<String,Integer> iNodes;
	private long bitMap;
	
	public FileSystem(){
		fileAllocationTable = new int[64];
		for (int i=0;i<64;i++){
			fileAllocationTable[i] = 0;
		}
		iNodes = new HashMap<String,Integer>();
		bitMap = 0x0000L;
	}
	
	public void setFileAllocation(int index, int pointer){
		fileAllocationTable[index] = pointer;
	}
	
	public int getFileAllocation(int index){
		return fileAllocationTable[index];
	}
	
	public int[] getFileAllocationTable(){
		return fileAllocationTable;
	}
	
	public HashMap<String,Integer> getINodes(){
		return iNodes;
	}
	
	public void addToINodes(String fileName, int startingBlock){
		iNodes.put(fileName, startingBlock);
	}
	
	public void deleteFromINodes(String fileName){
		iNodes.remove(fileName);
	}
	
	public void setBitMap(long bitmap){
		bitMap = bitmap;
	}
	
	public long getBitMap(){
		return bitMap;
	}
	
	public static void main(String[] args){
		
		FileSystem fat = new FileSystem();
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Enter exit or end to quit.");
		String input = "";
		while (!input.equalsIgnoreCase("-1")){
			System.out.print("> ");
			input = keyboard.nextLine();
			String[] command = input.split(" ");
			
			if (command[0].equalsIgnoreCase("put")){
				if (command.length == 2){
					String[] fileParameters = command[1].split(",");
					try{
						String fileName = fileParameters[0];
						Integer fileSize = Integer.parseInt(fileParameters[1]);
						if (fat.getINodes().containsKey(fileName)){
							System.out.println("That filename has already been used.");
						}
						else{
							String bitMapString = Long.toBinaryString(fat.getBitMap());
							//int bitMapLengthTest = bitMapString.length(); //Test value to see how many leading zeros are missing.
							if (bitMapString.length() < 64 && fat.getBitMap() != 0){
								String leadingZeros = "";
								int counter = 64 - bitMapString.length();
								for (int i=0;i<counter;i++){
									leadingZeros += "0";
								}
								bitMapString = leadingZeros + bitMapString;
								//bitMapLengthTest = bitMapString.length(); test value
							}
							for (int j=0;j<fileSize;j++){
								for (int i=0;i<bitMapString.length();i++){
									if (bitMapString.charAt(i) == '0'){
										long bitShift = 1l;
										bitShift = bitShift << (long)(63L-i);
							            long localBitMap = fat.getBitMap();
							            localBitMap = localBitMap | bitShift;
							            fat.setBitMap(localBitMap);
							            bitMapString = Long.toBinaryString(localBitMap);
							            //bitMapLengthTest = bitMapString.length();
							            if (j == 0){
							            	fat.addToINodes(fileName, i);
							            	fat.setFileAllocation(i, -1);
							            	j++;
							            	if (j == fileSize){
							            		break;
							            	}
							            }
							            else if (j == fileSize-1){
							            	int index = fat.getINodes().get(fileName);
							            	while (fat.getFileAllocation(index) != -1){
							            		index = fat.getFileAllocation(index);
							            	}
							            	fat.setFileAllocation(index, i);
							            	fat.setFileAllocation(i, -1);
							            	break;
							            }
							            else{
							            	int index = fat.getINodes().get(fileName);
							            	while (fat.getFileAllocation(index) != -1){
							            		index = fat.getFileAllocation(index);
							            	}
							            	fat.setFileAllocation(index, i);
							            	fat.setFileAllocation(i, -1);
							            	j++;
							            }
									}
								}
							}
						}
					} catch(Exception e){
						e.getMessage();
						System.out.println("The size was not a number.");
					}
				}
				else{
					System.out.println("The input was not correct.");
				}
			}
			
			else if (command[0].equalsIgnoreCase("del")){
				if (command.length < 2){ //No file specified.
					System.out.println("Please enter a fileName");
				}
				else{
					String fileName = command[1];
					boolean fileFound = fat.getINodes().containsKey(fileName);
					if (fileFound == true){
						int fileStart = fat.getINodes().get(fileName);
						int swap = fileStart;
						fat.deleteFromINodes(fileName);
						long localBitMap = fat.getBitMap();
						while (fileStart != -1){
							fileStart = fat.getFileAllocation(fileStart);
							fat.setFileAllocation(swap, 0);
							long bitShift = 1L;
							bitShift = bitShift << (long)(63L-swap);
							localBitMap = localBitMap ^ bitShift;
							
							//String testLocal = Long.toBinaryString(localBitMap);
							//String test = Long.toBinaryString(fat.getBitMap()); //Testing for error in bitmap. Hard to see in debug.
							//int lengthTest = test.length();
							
							fat.setBitMap(localBitMap);
							
						    //test = Long.toBinaryString(fat.getBitMap());
							//lengthTest = test.length();
							
							swap = fileStart;
						}
						
						
					}
					else{
						System.out.println("The file name was not found.");
					}
				}	
			}
			
			else if (command[0].equalsIgnoreCase("bitmap")){
				String bM = Long.toBinaryString(fat.getBitMap());
				String leadingZeros = "";
				if (bM.length() < 64){
					int counter = 64 - bM.length();
					for (int i=0;i<counter;i++){
						leadingZeros += "0";
					}
					bM = leadingZeros + bM;
				}
				char[] bitMap = bM.toCharArray();
				int blockNumber = 0;
				for (int i=0;i<bitMap.length;i++){
					if ( i % 8 == 0){
						System.out.print("\n" + blockNumber);
						if (i==8 || i== 0){
							System.out.print("  ");
						}
						else{
							System.out.print(" ");
						}
						System.out.print(bitMap[i]);
						blockNumber += 8;
					}
					else {
						System.out.print(bitMap[i]);
					}
				}
				System.out.println("");
			}
			
			else if (command[0].equalsIgnoreCase("inodes")){
				
				Iterator<Entry<String, Integer>> iterator = fat.getINodes().entrySet().iterator();
				while (iterator.hasNext()){
					Map.Entry<String,Integer> pair = (Map.Entry<String,Integer>) iterator.next();
					System.out.print(pair.getKey() + ": " + pair.getValue());
					int next = pair.getValue();
					if (fat.getFileAllocation(next) != -1){ // This if statement is for a file of size one or else it prints a 
						System.out.print(" -> ");
					}
					while(fat.getFileAllocation(next) != -1){
						System.out.print(fat.getFileAllocation(next));
						next = fat.getFileAllocation(next);
						if (fat.getFileAllocation(next) != -1){
							System.out.print(" -> ");
						}
						
					}
					//next = fat.getFileAllocation(next);
					System.out.print("\n");
				}
			}
			
			else if (command[0].equalsIgnoreCase("exit") || command[0].equalsIgnoreCase("end")){
				keyboard.close();
				System.exit(0);
				
			}
			else{
				System.out.println("Unknown command");
			}
		}	
	}
}
