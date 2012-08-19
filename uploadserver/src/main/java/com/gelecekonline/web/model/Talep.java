/*******************************************************************************
 * Copyright 2012 Muharrem Tac
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gelecekonline.web.model;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class Talep {

	private String dosyaAdi;
	private CommonsMultipartFile dosya;
	
	public String getDosyaAdi() {
		return dosyaAdi;
	}
	public void setDosyaAdi(String dosyaAdi) {
		this.dosyaAdi = dosyaAdi;
	}
	public CommonsMultipartFile getDosya() {
		return dosya;
	}
	public void setDosya(CommonsMultipartFile dosya) {
		this.dosya = dosya;
	}


}
