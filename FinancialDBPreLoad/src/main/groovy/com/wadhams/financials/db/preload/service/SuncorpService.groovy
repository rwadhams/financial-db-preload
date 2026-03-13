package com.wadhams.financials.db.preload.service

import java.text.NumberFormat

import com.wadhams.financials.db.preload.dto.SuncorpDTO
import com.wadhams.financials.db.preload.type.Status

class SuncorpService {
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	
	List<SuncorpDTO> filterDTOList(List<SuncorpDTO> suncorpDTOList, Status status) {
		return suncorpDTOList.findAll {dto -> dto.csvStatus == status}
	}
	
	def reportBypassItems(List<SuncorpDTO> bypassDTOList) {
		println 'Verify that all items below really should have been bypassed:'
		bypassDTOList.each {dto ->
			println "\tDate: ${dto.transactionDate}, Amount: ${cf.format(dto.amount)}, Description: ${dto.description}}"
		}
	}
	
	def writeValidItemFile(List<SuncorpDTO> validDTOList) {
		File fout = new File("out/financial.xml")
		PrintWriter pw = fout.newPrintWriter()
		pw.print '<financials>'
		
		validDTOList.each {dto ->
			pw.print '<data>'
			
			//transactionDate
			pw.print "<dt>${dto.transactionDate}</dt>"
			
			//amount
			pw.print "<amt>${dto.amount}</amt>"
			
			pw.print "<payee>${dto.payee}</payee>"
			
			//description
			pw.print "<desc>${dto.description}</desc>"
			
			pw.print '<asset></asset>'
			
			//category
			pw.print "<cat>${dto.category}</cat>"
			
			//large transaction amounts annotate RG3
//			String rg3 = ''
//			if (dto.amount > 100) {
//				rg3 = '$$$'
//			}
//			pw.print "<subcat></subcat><start></start><end></end><rg1></rg1><rg2></rg2><rg3>$rg3</rg3></data>"
			
			pw.print "<subcat></subcat><start></start><end></end><rg1></rg1><rg2></rg2><rg3></rg3></data>"
		}
		pw.println '</financials>'
		pw.close()
	}
}
