package br.com.ufpi.priorization.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;

public class DataGenerator {

	private static final String FOLDER_GENERATE_MATHEUS = "H:/priorizacao-teste/Arquivos Base/AleatoriosDistNormal/2014_06_19/";
	private final static String BASE_FOLDER = FOLDER_GENERATE_MATHEUS;
	

	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
	private static Random random = new Random(1094849340978620l);
	private final static int MAXIMO = 10;
	private final static int CTR = 50;
	private final static int STD = 15;

	public static void main(String[] args) throws IOException {

		// config
		boolean useArray = true;

		// config por array
		int[] classes = { 2, 4, 8, 16 }; // 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768
		int[] reqs = { 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100 };

		// config por fator
		int classe = 100000;
		int clInit = 5;
		int clFator = 5;
		int clMax = 100;
		
		// generate files
		if (useArray) {
			for (int i = 0; i < classes.length; i++) {
				for (int j = 0; j < reqs.length; j++) {
					generetaFiles(classes[i], reqs[j]);
				}
			}
		} else {
			for (int i = clInit; i <= clMax; i = i + clFator) {
				generetaFiles(classe, i);
			}
		}

	}

	public static void generetaFiles(int contClass, int reqCount)
			throws IOException {
		String folder = BASE_FOLDER + "cl_" + contClass + "_req_" + reqCount
				+ "/";

		File dir = new File(folder);
		try {
			// ve se ja existe a pasta e tenta criar
			boolean canSave = false;
			if (!dir.exists()) {
				boolean result = dir.mkdir();
				if (result) {
					canSave = true;
				}
			} else {
				canSave = true;
			}

			// se foi criada a pasta
			if (canSave) {

				generateSQFD(contClass, reqCount, 5, folder + "New-SQFD-cl-" + contClass + "-req-" + reqCount + ".csv");
				generateTest(contClass, reqCount, folder + "New-TestCoverage-cl-" + contClass + "-req-" + reqCount + ".csv");
				generateCoupling(contClass, folder + "New-Coupling-cl-" + contClass + ".csv", 10);
				generateAleatoryComplexity(contClass, folder + "New-Complexity-cl-" + contClass + ".csv", 10);

				// salvar log com data de criacao
				File log = new File(folder + "_log_" + dateFormat.format(new Date()) + ".txt");
				log.createNewFile();

			} else {
				System.out.println("### - Não conseguiu criar a pasta: " + folder);
			}
		} catch (Exception e) {
			System.out.println("##E - Não conseguiu criar a pasta: " + folder);
			e.printStackTrace();
		}
	}

	private static void generateCoupling(int numberOfClass, String addressFile,
			Integer normalizeNumber) throws IOException {
		List<String[]> data = new ArrayList<String[]>();
		CSVWriter writer = new CSVWriter(new FileWriter(addressFile), ';');
		/* Header */
		String[] header = new String[numberOfClass];
		for (int i = 0; i < numberOfClass; i++) {
			header[i] = "Class[" + (i + 1) + "]";
		}
		data.add(header);
		/* Body */
		Double major = Double.MIN_VALUE;
		String[] body = new String[numberOfClass];
		List<Double> allImportanceValue = getAllNormalizedValues(numberOfClass, MAXIMO, CTR, STD);
		for (int i = 0; i < numberOfClass; i++) {
			Double actual = new Double(String.format("%.0f", allImportanceValue.get(i)));
			if (major < actual) {
				major = actual;
			}
			body[i] = actual.toString();
		}

		// normalize
		if (normalizeNumber != null) {
			body = new String[numberOfClass];
			for (int i = 0; i < numberOfClass; i++) {
				Double actual = (allImportanceValue.get(i) / major) * normalizeNumber;
				body[i] = String.format("%.0f", actual);
			}
		}

		data.add(body);

		writer.writeAll(data);
		writer.close();
	}

	public static void generateAleatoryComplexity(int numberOfClass,
			String addressFile, Integer normalizeNumber) throws IOException {
		List<String[]> data = new ArrayList<String[]>();
		CSVWriter writer = new CSVWriter(new FileWriter(addressFile), ';');
		/* Header */
		String[] header = new String[numberOfClass];
		for (int i = 0; i < numberOfClass; i++) {
			header[i] = "Class[" + (i + 1) + "]";
		}
		data.add(header);
		/* Body */
		String[] body = new String[numberOfClass];
		// complexidade maxima = 25
		Double major = Double.MIN_VALUE;
		List<Double> allCiclomaticComplexityValue = getAllNormalizedValues(numberOfClass, MAXIMO, CTR, STD);
		for (int i = 0; i < numberOfClass; i++) {
			Double actual = new Double(String.format("%.0f", allCiclomaticComplexityValue.get(i)));
			if (major < actual) {
				major = actual;
			}
			body[i] = actual.toString();
		}

		// normalize
		if (normalizeNumber != null) {
			body = new String[numberOfClass];
			for (int i = 0; i < numberOfClass; i++) {
				Double actual = (allCiclomaticComplexityValue.get(i) / major) * normalizeNumber;
				body[i] = String.format("%.0f", actual);
			}
		}

		data.add(body);

		writer.writeAll(data);
		writer.close();
	}

	public static void generateSQFD(int numberOfClass, int numberOfRequirement,
			int countClient, String addressFile) throws IOException {

		String[] stringLine;
		List<String[]> data = new ArrayList<String[]>();

		CSVWriter writer = new CSVWriter(new FileWriter(addressFile), ';');

		// add 2 to generate client priority column and description
		numberOfClass += 1 + countClient;
		// add 1 to generate description
		numberOfRequirement++;
		// each line
		for (int req = 0; req < numberOfRequirement; req++) {
			stringLine = new String[numberOfClass];
			List<Double> allImportanceValue = getAllNormalizedValues(numberOfClass, MAXIMO, CTR, STD);
			// each column
			for (int clas = 0; clas < numberOfClass; clas++) {
				// title of first row
				if (req == 0) {
					if (clas == 0) {
						stringLine[clas] = "Requirement\\Class";
					} else if (clas < numberOfClass - countClient) {
						stringLine[clas] = "Class[" + clas + "]";
					} else {
						stringLine[clas] = "Client Priority";
					}
				} else {
					// title of row
					if (clas == 0) {
						stringLine[clas] = "Requirement[" + req + "]";
					} else if (clas < numberOfClass - countClient) {
						stringLine[clas] = getCorrelationValue(0, 15);
					} else {
						stringLine[clas] = String.format("%.0f", allImportanceValue.get(clas));
					}

				}
			}
			data.add(stringLine);
		}

		writer.writeAll(data);
		writer.close();
	}
	
	

	public static void generateTest(int numberOfClass, int numberOfTests,
			String addressFile) throws IOException {
		String[] stringLine;

		CSVWriter writer = new CSVWriter(new FileWriter(addressFile), ';');

		// add 2 to generate client priority column and description
		numberOfClass += 2;
		// add 1 to generate description
		numberOfTests++;

		double countCoverage, coverage;

		// each line
		for (int test = 0; test < numberOfTests; test++) {
			stringLine = new String[numberOfClass];
			countCoverage = 0;
			// each column
			for (int clas = 0; clas < numberOfClass; clas++) {
				// title of first row
				if (test == 0) {
					if (clas == 0) {
						stringLine[clas] = "Test\\Class";
					} else if (clas < numberOfClass - 1) {
						stringLine[clas] = "Class[" + clas + "]";
					} else {
						stringLine[clas] = "Temp";
					}
				} else {
					// title of row
					if (clas == 0) {
						stringLine[clas] = "Test[" + test + "]";
					} else {
						if (clas < numberOfClass - 1) {
							coverage = getTestValue(50, 20);
							countCoverage += coverage;
							stringLine[clas] = String.format("%.4f", coverage);
						} else {
							stringLine[clas] = String.format("%.0f", getTestValue(50, 20) * 100);
						}
					}
				}
			}
			writer.writeNext(stringLine);
		}

		writer.close();
	}

	/**
	 * Retorna todas as importancias normalizadas a partir da quantidade
	 * passada, do valor central e do desvio padrão
	 * 
	 * @param cont
	 * @param ctr
	 * @param std
	 * @return
	 */
	public static List<Double> getAllNormalizedValues(int cont, int maximum,
			int ctr, int std) {
		List<Double> list = new ArrayList<Double>();
		double max = Integer.MIN_VALUE, atual;
		for (int i = 0; i < cont; i++) {
			atual = generateGaussianNumber(ctr, std, false);
			if (atual > max) {
				max = atual;
			}
			list.add(atual);
		}

		List<Double> finalList = new ArrayList<Double>();
		for (Double d : list) {
			finalList.add((d / max) * maximum);
		}
		return finalList;
	}

	/**
	 * Retorna o valor da cobertura do teste
	 * 
	 * @param ctr
	 * @param std
	 * @return
	 */
	public static Double getTestValue(int ctr, int std) {
		double coverage = generateGaussianNumber(ctr, std, false);
		coverage = coverage < 0 ? 0 : coverage;
		coverage = coverage > 100 ? 100 : coverage;
		return coverage;
	}

	/**
	 * Retorn o valor da correlação a partir do valor central e do desvio
	 * padrão.
	 * 
	 * @param ctr
	 * @param std
	 * @return
	 */
	public static String getCorrelationValue(int ctr, int std) {
		double val = generateGaussianNumber(ctr, std, false);
		String retorno;
		if (val > ctr - std && val < ctr + std) {
			retorno = "0";
		} else if (val >= ctr + std && val < ctr + (2 * std)) {
			retorno = "1";
		} else if (val <= ctr - std && val > ctr - (2 * std)) {
			retorno = "3";
		} else {
			retorno = "9";
		}
		return retorno;
	}

	/**
	 * Tornar o random mais seguro. Descontinuado, pois aA ideia é justamente tornar os dados "repetitíveis"  
	 * @return
	 */
	public static long getLongSeed() {
		  SecureRandom sec = new SecureRandom();
		  byte[] sbuf = sec.generateSeed(8);
		  ByteBuffer bb = ByteBuffer.wrap(sbuf);
		  return bb.getLong();
	}
	
	public static double generateGaussianNumber(int centerValue,
			float standardDeviation, boolean invert) {
		double val, dif;
		val = random.nextGaussian() * standardDeviation + centerValue;
		dif = (centerValue - val);
		// generate values larger than centerValue
		if (invert) {
			dif = dif < 0 ? -dif : dif;
		}
		val = centerValue + dif;
		return val;
	}

	public static int getPoisson(double lambda) {
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;

		do {
			k++;
			p *= Math.random();
		} while (p > L);

		return k - 1;
	}

	public static int getBinomial(int n, double p) {
		int x = 0;
		for (int i = 0; i < n; i++) {
			if (Math.random() < p){
				x++;
			}
		}
		return x;
	}
	
	
	/**
	 * method to verify how the data are behaving
	 * 
	 */
	public void testDistribution(){
		HashMap<String, Integer> count = new HashMap<String, Integer>();
		for(int i = 0; i < 1000; i ++){
			String key = getCorrelationValue(0, 15);
			if(count.get(key) != null){
				count.put(key, count.get(key) + 1);
			}else{
				count.put(key, 1);
			}
		}
		
		Set<String> keySet = count.keySet();
		for(String key : keySet){
			System.out.println(key+"- "+count.get(key));
		}
	}
}
