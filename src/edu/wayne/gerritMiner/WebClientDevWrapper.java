/*
 * Copyright (C) 2018 Wayne State University SEVERE Lab
 *
 * Author: Amiangshu Bosu
 *
 * Licensed under GNU LESSER GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.wayne.gerritMiner;
import java.io.IOException;
import java.security.cert.X509Certificate;


import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;

/*
This code is public domain: you are free to use, link and/or modify it in any way you want, for all purposes including commercial applications. 
*/
public class WebClientDevWrapper{
	
	public static HttpClient wrapClient(HttpClient base) {
		try {
				SSLContext ctx = SSLContext.getInstance("TLS");
				X509TrustManager tm = new X509TrustManager() {
					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] arg0,
							String arg1) throws java.security.cert.CertificateException {
						// TODO Auto-generated method stub
						
					}
					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] arg0,
							String arg1) throws java.security.cert.CertificateException {
						// TODO Auto-generated method stub
						
					}
					@Override
					public X509Certificate[] getAcceptedIssuers() {
						// TODO Auto-generated method stub
						return null;
					}
				};

					
				X509HostnameVerifier verifier = new X509HostnameVerifier() {
					@Override
					public void verify(String string, X509Certificate xc) throws SSLException{}
	
					@Override
						public void verify(String string, String[] strings, String[] strings1) throws SSLException {
					}
	
					@Override
					public boolean verify(String string, SSLSession ssls) {
						return true;
					}
	
					@Override
					public void verify(String arg0, SSLSocket arg1)
							throws IOException {
						// TODO Auto-generated method stub
						
					}
				};
				ctx.init(null, new TrustManager[]{tm}, null);
				SSLSocketFactory ssf = new SSLSocketFactory(ctx);
				ssf.setHostnameVerifier(verifier);
				ClientConnectionManager ccm = base.getConnectionManager();
				SchemeRegistry sr = ccm.getSchemeRegistry();
				sr.register(new Scheme("https", ssf, 443));
				return new DefaultHttpClient(ccm, base.getParams());
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	
}