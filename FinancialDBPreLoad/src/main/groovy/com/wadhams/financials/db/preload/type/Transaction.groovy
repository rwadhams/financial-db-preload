package com.wadhams.financials.db.preload.type

enum Transaction {
	Suncorp('SUNCORP'),
	Wise('WISE'),
	Unknown('Unknown');
	
	private static EnumSet<Transaction> allEnums = EnumSet.allOf(Transaction.class)
	
	private final String name

	Transaction(String name) {
		this.name = name
	}
	
	public static Transaction findByName(String text) {
		if (text) {
			text = text.toUpperCase()
			for (Transaction e : allEnums) {
				if (e.name.equals(text)) {
					return e
				}
			}
		}
		else {
			return Transaction.Unknown
		}
		return Transaction.Unknown
	}

}
