package sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SalesSummary {
	public static void main(String[] args) {

		Map<String, String> branchMap = new HashMap<>(); // branchMapの格納
		Map<String, Integer> branchSales = new HashMap<>(); // 店舗別売上げ合計の格納
		Map<String, String> commodityMap = new HashMap<>(); // commodityMapの格納
		Map<String, Integer> commoditySales = new HashMap<>(); // 商品別売上げ合計の格納

		// リストをつくる（args、読み込みファイル名、売上合計の格納先、リストの格納先、エラー表示名）
		try {
			if (makeLists(args[0], "branch.lst", 3, branchSales, branchMap, "支店")) {
			} else {
				return;
			}
			if (makeLists(args[0], "commodity.lst", 8, commoditySales, commodityMap,
					"商品")) {
			} else {
				return;
			}
		} catch (Exception e) {
			return;
		}

		// ファイル名の連番確認用リスト
		List<String> salesCheckList = new ArrayList<>();
		File[] files = new File(args[0]).listFiles();
		String fileName = null;
		for (int i = 0; i < files.length; i++) {
			List<String> salesList = new ArrayList<>(); // 売上げリストの格納
			fileName = files[i].getName();
			if (files[i].isFile() && fileName.endsWith(".rcd")) {
				// ファイル名の連番チェック
				int lastPosition = fileName.lastIndexOf('.');
				if (lastPosition == 8) {
					String checkName;
					checkName = fileName.substring(0, lastPosition);
					salesCheckList.add(checkName);
				}
			}
			for (int j = 0; j < salesCheckList.size(); j++) {
				try {
					int name = Integer.parseInt(salesCheckList.get(j));
					if (name != (j + 1)) {
						System.out.println("売上ファイル名が連番になっていません"); // error
						return;
					}
				} catch (NumberFormatException e) {
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}
			}

			// ファイル名の連番チェック(1
			if (files[i].isFile() && fileName.endsWith(".rcd")) {
				// 売上リストの取得
				BufferedReader sales = null;
				try {
					sales = new BufferedReader(new FileReader(files[i]));
					int n = 0;
					String line = "";
					while ((line = sales.readLine()) != null) {
						++n;
						if (n > 3) {
							System.out.println(fileName + "のフォーマットが不正です"); // error
							return;
						}
						salesList.add(line);

					}
				} catch (Exception e) {
					System.out.println("売上ファイル読み込み時にエラーが発生しました");
				} finally {
					try {
						sales.close();
					} catch (IOException e) {
						System.out.println("ファイルを閉じる際にエラーが発生しました");
					}
				}
			}

			// 売上の集計
			if (total(branchSales, salesList, 0, fileName, "店舗")) {
			} else {
				return;
			}
			if (total(commoditySales, salesList, 1, fileName, "商品")) {
			} else {
				return;
			}

			// 集計の出力(args、リスト、売上合計、出力ファイル名）
			if (output(args[0], branchMap, branchSales, "branch.out")) {
			} else {
				return;
			}
			if (output(args[0], commodityMap, commoditySales, "commodity.out")) {
			} else {
				return;
			}
		}
	}

	// リストの作成
	private static boolean makeLists(String arg, String listFileName, int keta,
			Map<String, Integer> salesMap, Map<String, String> listMap,
			String errorWord) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(arg, listFileName)));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] bra = line.split(",", -1);
			
				if(listFileName == "branch.lst")
					try {
				        Integer.parseInt(bra[0]);
			        } catch (NumberFormatException e) {
			        	System.out.println(errorWord + "定義ファイルのフォーマットが不正です"); // error
			        	return false;
				    }
				if(listFileName == "commodity.lst")
					if(bra[0].matches("^[a-zA-Z0-9]+$")){	
					} else {
						System.out.println(errorWord + "定義ファイルのフォーマットが不正です"); // error
			        	return false;
					}

				if (bra[0].length() != keta && bra.length != 2) {
					System.out.println(errorWord + "定義ファイルのフォーマットが不正です"); // error
					return false;
				}salesMap.put(bra[0], 0);
				listMap.put(bra[0], bra[1]);
			}
		} catch (FileNotFoundException e) {
			System.out.println(errorWord + "ファイルが存在しません"); // error
			return false;
		} finally {
			br.close();
		}
		return true;
	}

	// 集計
	private static boolean total(Map<String, Integer> salesMap,
			List<String> salesList, int index, String fileName, String codeName) {
		try {
			int sales = 0;
			if (salesMap.get(salesList.get(index)) != null) {
				sales = salesMap.get(salesList.get(index));
			} else {
				System.out.println(fileName + "の" + codeName + "コードが不正です"); // error
				return false;
			}

			int salesValue = sales + Integer.parseInt(salesList.get(2));
			salesMap.put(salesList.get(index), salesValue);
			String valueStr = Integer.toString(salesValue);

			// 桁数チェック
			if (valueStr.length() > 10) {
				System.out.println("合計金額が10桁を超えました"); // error
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	// ファイルのアウトプット
	private static boolean output(String arg, Map<String, String> listMap,
			Map<String, Integer> salesMap, String outFileName) {

		List<Entry<String, Integer>> ent = new ArrayList<>(salesMap.entrySet());
		Collections.sort(ent, new Comparator<Entry<String, Integer>>() { // 降順にソート
					public int compare(Entry<String, Integer> o1,
							Entry<String, Integer> o2) {
						return o2.getValue().compareTo(o1.getValue());
					}
				});

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(
					arg, outFileName))));
			for (Entry<String, Integer> e : ent) {
				pw.println(e.getKey() + "," + listMap.get(e.getKey()) + ","
						+ e.getValue());
			}
		} catch (IOException e) {
			System.out.println("データの出力時にエラーが発生しました"); // error
			return false;
		} finally {
			pw.close();
		}
		return true;
	}
}