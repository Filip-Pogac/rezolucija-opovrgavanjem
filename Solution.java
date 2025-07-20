package ui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Solution {

	public static void main(String... args) throws Exception {
		if (args[0].equalsIgnoreCase("resolution")) {
			rezolucijaOpovrgavanjem(citajKlauzule(args[1]));
		} else if (args[0].equalsIgnoreCase("cooking")) {
			kuharskiAsistent(citajKlauzule(args[1]), citajNaredbe(args[2]));
		}
	}

	private static void rezolucijaOpovrgavanjem(HashMap<Integer, Set<String>> klauzule) {
		HashMap<Integer, Set<String>> ulazneKlauzule = new HashMap<>(klauzule);
		int rBr = ulazneKlauzule.size();

		HashMap<Integer, Set<String>> skupPotpore = new HashMap<>();
		int najveciKljuc = Collections.max(ulazneKlauzule.keySet());
		Set<String> cilj = new HashSet<>(ulazneKlauzule.get(najveciKljuc));
		Set<String> ciljPrijeIzmjena = new HashSet<>(cilj);
		ulazneKlauzule.remove(najveciKljuc);

		Map<Integer, int[]> roditelji = new HashMap<>();
		LinkedHashSet<Integer> neizvedeneKlauzule = new LinkedHashSet<>(ulazneKlauzule.keySet());

		for (String c : cilj) {
			Set<String> negirana = new HashSet<>();
			if (c.contains("~")) {
				negirana.add(c.replace("~", ""));
			} else {
				negirana.add("~" + c);
			}
			skupPotpore.put(++rBr, negirana);
			neizvedeneKlauzule.add(rBr);
		}

		Map<Integer, Set<String>> sveKlauzule = new HashMap<>();
		sveKlauzule.putAll(ulazneKlauzule);
		sveKlauzule.putAll(skupPotpore);

		while (true) {
			HashMap<Integer, Set<String>> noveRezolvente = new HashMap<>();

			List<Integer> kljuceviZaBrisanjeIzUlaznih = new ArrayList<>();
			List<Integer> kljuceviZaBrisanjeIzSkupa = new ArrayList<>();

			for (Map.Entry<Integer, Set<String>> c1 : ulazneKlauzule.entrySet()) {
				for (Map.Entry<Integer, Set<String>> c2 : skupPotpore.entrySet()) {
					//strategija brisanja
					if (c1.getValue().containsAll(c2.getValue())) {
						kljuceviZaBrisanjeIzUlaznih.add(c1.getKey());
					}
					if (c2.getValue().containsAll(c1.getValue())) {
						kljuceviZaBrisanjeIzSkupa.add(c2.getKey());
					}
					for (String s1 : c1.getValue()) {
						if (s1.contains("~") && c1.getValue().contains(s1.replace("~", ""))) {
							kljuceviZaBrisanjeIzUlaznih.add(c1.getKey());
						}
					}
					for (String s2 : c2.getValue()) {
						if (s2.contains("~") && c2.getValue().contains(s2.replace("~", ""))) {
							kljuceviZaBrisanjeIzSkupa.add(c2.getKey());
						}
					}
				}
			}

			for (Integer k : kljuceviZaBrisanjeIzUlaznih) {
				ulazneKlauzule.remove(k);
			}
			for (Integer k : kljuceviZaBrisanjeIzSkupa) {
				skupPotpore.remove(k);
			}

			List<Integer> kljuceviZaBrisanjeIzSkupa2 = new ArrayList<>();

			for (Map.Entry<Integer, Set<String>> c1 : skupPotpore.entrySet()) {
				for (Map.Entry<Integer, Set<String>> c2 : skupPotpore.entrySet()) {

					if (c1.getKey().equals(c2.getKey())) {
						continue;
					}
					//strategija brisanja
					if (c1.getValue().containsAll(c2.getValue())) {
						kljuceviZaBrisanjeIzSkupa2.add(c1.getKey());
					}
					if (c2.getValue().containsAll(c1.getValue())) {
						kljuceviZaBrisanjeIzSkupa2.add(c2.getKey());
					}
					for (String s1 : c1.getValue()) {
						if (s1.contains("~") && c1.getValue().contains(s1.replace("~", ""))) {
							kljuceviZaBrisanjeIzSkupa2.add(c1.getKey());
						}
					}
					for (String s2 : c2.getValue()) {
						if (s2.contains("~") && c2.getValue().contains(s2.replace("~", ""))) {
							kljuceviZaBrisanjeIzSkupa2.add(c2.getKey());
						}
					}

				}
			}
			for (Integer k : kljuceviZaBrisanjeIzSkupa2) {
				skupPotpore.remove(k);
			}


			for (Map.Entry<Integer, Set<String>> c1 : ulazneKlauzule.entrySet()) {
				for (Map.Entry<Integer, Set<String>> c2 : skupPotpore.entrySet()) {
					for (String literal1 : c1.getValue()) {
						for (String literal2 : c2.getValue()) {
							if ((literal1.contains("~") && literal2.equals(literal1.replace("~", ""))) ||
									(literal2.contains("~") && literal1.equals(literal2.replace("~", "")))) {
								if(c1.getValue().size() == 1 && c2.getValue().size() == 1){
									rBr++;
									sveKlauzule.put(rBr, Collections.singleton("NIL"));
									roditelji.put(rBr, new int[]{c1.getKey(), c2.getKey()});
									ispis(rBr, roditelji, neizvedeneKlauzule, sveKlauzule);
									System.out.printf("[CONCLUSION]: %s is true\n\n", String.join(" v ", ciljPrijeIzmjena));
									return;


								}
								rBr++;
								Set<String> rezultat = new HashSet<>();
								rezultat.addAll(c1.getValue());
								rezultat.addAll(c2.getValue());
								rezultat.remove(literal1);
								rezultat.remove(literal2);
								if (!sveKlauzule.containsValue(rezultat)) {
									noveRezolvente.put(rBr, rezultat);
									sveKlauzule.put(rBr, rezultat);
									roditelji.put(rBr, new int[]{c1.getKey(), c2.getKey()});
								}
							}
						}
					}
				}
			}

			for (Map.Entry<Integer, Set<String>> c1 : skupPotpore.entrySet()) {
				for (Map.Entry<Integer, Set<String>> c2 : skupPotpore.entrySet()) {
					if (c1.getKey().equals(c2.getKey())) continue;

					for (String literal1 : c1.getValue()) {
						for (String literal2 : c2.getValue()) {
							if ((literal1.contains("~") && literal2.equals(literal1.replace("~", ""))) ||
									(literal2.contains("~") && literal1.equals(literal2.replace("~", "")))) {
								if(c1.getValue().size() == 1 && c2.getValue().size() == 1){
									rBr++;
									sveKlauzule.put(rBr, Collections.singleton("NIL"));
									roditelji.put(rBr, new int[]{c1.getKey(), c2.getKey()});
									ispis(rBr, roditelji, neizvedeneKlauzule, sveKlauzule);
									System.out.printf("[CONCLUSION]: %s is true\n\n", String.join(" v ", ciljPrijeIzmjena));
									return;


								}
								rBr++;
								Set<String> rezultat = new HashSet<>();
								rezultat.addAll(c1.getValue());
								rezultat.addAll(c2.getValue());
								rezultat.remove(literal1);
								rezultat.remove(literal2);
								if (!sveKlauzule.containsValue(rezultat)) {
									noveRezolvente.put(rBr, rezultat);
									sveKlauzule.put(rBr, rezultat);
									roditelji.put(rBr, new int[]{c1.getKey(), c2.getKey()});
								}
							}
						}
					}
				}
			}

			if (noveRezolvente.isEmpty()) {
				System.out.printf("[CONCLUSION]: %s is unknown\n\n", String.join(" v ", ciljPrijeIzmjena));
				return;
			}

            skupPotpore.putAll(noveRezolvente);
		}
	}

	private static void ispis(int rBrNIL, Map<Integer, int[]> roditelji, Set<Integer> neizvedeneKlauzule, Map<Integer, Set<String>> sveKlauzule) {
		Set<Integer> koristeno = new TreeSet<>();
		Queue<Integer> red = new LinkedList<>();
		red.offer(rBrNIL);

		while (red.size() > 0) {
			int trenutni = red.remove();
			if (koristeno.contains(trenutni))
				continue;
			else
				koristeno.add(trenutni);

			int[] roditelj = roditelji.get(trenutni);
			if (roditelj == null)
				continue;
			else {
				red.offer(roditelj[0]);
				red.offer(roditelj[1]);
			}

		}

		int i=0;
		for (int br : koristeno) {
			if (neizvedeneKlauzule.contains(br))
				System.out.printf("%d. %s\n", br, String.join(" v ", sveKlauzule.get(br)));
			else {
				if (i++ == 0){
					System.out.println("==============================");
				}
					System.out.printf("%d. %s (%d, %d)\n", br, String.join(" v ", sveKlauzule.get(br)), roditelji.get(br)[0], roditelji.get(br)[1]);
			}
		}
	}



	private static void kuharskiAsistent(HashMap<Integer, Set<String>> ulazneKlauzule, LinkedHashMap<Integer, LinkedHashSet<String>> mapaNaredbi) {
		HashMap<Integer, Set<String>> klauzule = ulazneKlauzule;
		LinkedHashMap<Integer, LinkedHashSet<String>> naredbe = mapaNaredbi;
		int rBr = klauzule.size();
		for (LinkedHashSet<String> n : naredbe.values()) {
			System.out.println("Naredba: " + n +"\n");
			String zadnjiElement = null;
			for (String element : n) {
				zadnjiElement = element;
			}
			switch (zadnjiElement) {
				case "?":
					HashMap<Integer, Set<String>> tempKlauzule = new HashMap<>();
					for (Map.Entry<Integer, Set<String>> entry : klauzule.entrySet()) {
						tempKlauzule.put(entry.getKey(), new HashSet<>(entry.getValue()));
					}
					rBr++;
					n.remove("?");
					tempKlauzule.put(rBr, n);
					rezolucijaOpovrgavanjem(tempKlauzule);
					rBr--;
					break;
				case "+":
					rBr++;
					n.remove("+");
					klauzule.put(rBr, n);
					break;
				case "-":
					n.remove("-");
					HashMap<Integer, Set<String>> tempKlauzule2 = new HashMap<>(klauzule);
					for(Map.Entry<Integer, Set<String>> klauzula : tempKlauzule2.entrySet()){
						klauzula.getValue().removeAll(n);
						if(klauzula.getValue().isEmpty()){
							klauzule.remove(klauzula.getKey());
						}
					}
					break;
			}
		}
	}



	private static HashMap<Integer, Set<String>> citajKlauzule(String putanjaDatoteke) throws IOException {
		HashMap<Integer, Set<String>> literali = new HashMap<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(
						new BufferedInputStream(
								new FileInputStream(putanjaDatoteke)), StandardCharsets.UTF_8))) {
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) continue;
				i++;
				literali.put(i, new HashSet<>(Arrays.asList(line.toLowerCase().split(" v "))));
			}
			return literali;
		}
	}

	private static LinkedHashMap<Integer, LinkedHashSet<String>> citajNaredbe(String putanjaDatoteke) throws IOException {
		LinkedHashMap<Integer, LinkedHashSet<String>> literali = new LinkedHashMap<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(
						new BufferedInputStream(
								new FileInputStream(putanjaDatoteke)), StandardCharsets.UTF_8))) {
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) continue;
				i++;
				literali.put(i, new LinkedHashSet<>(Arrays.asList(line.toLowerCase().split(" v | "))));
			}
			return literali;
		}
	}
}



