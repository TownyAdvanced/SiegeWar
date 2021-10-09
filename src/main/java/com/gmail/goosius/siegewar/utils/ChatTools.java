package com.gmail.goosius.siegewar.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatTools {

	public static List<String> listArr(String[] args, String prefix) {

		return list(Arrays.asList(args), prefix);
	}

	public static List<String> list(List<String> args) {

		return list(args, "");
	}

	public static List<String> list(List<String> args, String prefix) {
		if (args.size() > 0) {
			List<String> out = new ArrayList<>();
			out.add(prefix + String.join(", ", args));
			return out;
		} else {
			return new ArrayList<>();
		}

	}	
}
