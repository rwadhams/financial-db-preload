package com.wadhams.financials.db.preload.controller

import com.wadhams.financials.db.preload.dto.SuncorpDTO
import com.wadhams.financials.db.preload.service.SuncorpCSVService
import com.wadhams.financials.db.preload.service.SuncorpService
import com.wadhams.financials.db.preload.type.Status


class ConvertSuncorpDataController {
	def execute() {
		SuncorpCSVService suncorpCSVService = new SuncorpCSVService()
		
		File csvFile = suncorpCSVService.getDataFile()
		
		List<SuncorpDTO> suncorpDTOList = suncorpCSVService.buildSuncorpDTOList(csvFile)
		println "suncorpDTOList size: ${suncorpDTOList.size()}"
		println ''
		
		SuncorpService suncorpService = new SuncorpService()
		//filter into two lists
		List<SuncorpDTO> validDTOList = suncorpService.filterDTOList(suncorpDTOList, Status.Valid)
		println "validDTOList size: ${validDTOList.size()}"
		List<SuncorpDTO> bypassDTOList = suncorpService.filterDTOList(suncorpDTOList, Status.Bypass)
		println "bypassDTOList size: ${bypassDTOList.size()}"
		println ''
		
		suncorpService.reportBypassItems(bypassDTOList)

		suncorpService.writeValidItemFile(validDTOList)
		
		suncorpCSVService.backupDataFile(csvFile)
	}
}
