package ru.ifmo.se.droidscan;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

	//в res/values/strings.xml лежит конфигурация

	TextView tvEnabledGPS;
	TextView tvStatusGPS;
	TextView tvLocationGPS;
	TextView tvEnabledNet;
	TextView tvStatusNet;
	TextView tvLocationNet;
	TextView tvFile;

	private LocationManager locationManager;
	StringBuilder sbGPS = new StringBuilder();
	StringBuilder sbNet = new StringBuilder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS); // получаем textView компоненты
		tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
		tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
		tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
		tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
		tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);
		tvFile = (TextView) findViewById(R.id.tvFile);
		tvFile.setMovementMethod(new ScrollingMovementMethod());

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);//получаем LocationManager, через который и работаем
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,//вешаем слушателя
				1000 * 60, 0, locationListener);//в параметрах тип провайдера(GPS_PROVIDER или NETWORK_PROVIDER), минимально время в миллисекундах, минимальное расстояние в метрах для изменения координат, слушатель
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000 * 60, 10,
				locationListener);
		checkEnabled();
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);//отключаем слушателя
	}

	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {//новые данные о местоположении
			showLocation(location);//наш метод, который на экране отображает данные
		}

		@Override
		public void onProviderDisabled(String provider) {//указанный провайдер был отключен юзером
			checkEnabled();//наш метод, который на экране обновит текущий статус провайдера
		}

		@Override
		public void onProviderEnabled(String provider) {//указанный провайдер был включен
			checkEnabled();//обновление статуса
			showLocation(locationManager.getLastKnownLocation(provider));//запрашиваем последнее доступное местоположение от провайдера
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {//изменился статус указанного провайдера
			if (provider.equals(LocationManager.GPS_PROVIDER)) {//OUT_OF_SERVICE - данные будут недоступны долгое время
				tvStatusGPS.setText("Status: " + String.valueOf(status));//TEMPORARILY_UNAVAILABLE - данные временно не доступны
			} else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {//AVAILABLE - данные доступны
				tvStatusNet.setText("Status: " + String.valueOf(status));
			}
		}
	};

	private void showLocation(Location location) {//показываем новые данные
		if (location == null)
			return;
		if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {//определяем провайдера и отображаем данные в нужном поле
			tvLocationGPS.setText(formatLocation(location));
		} else if (location.getProvider().equals(
				LocationManager.NETWORK_PROVIDER)) {
			tvLocationNet.setText(formatLocation(location));
		}
	}

	private String formatLocation(Location location) {//форматирует данные в строку: широта, долгота, время определения
		if (location == null)
			return "";
		else {
			String str = String.format(
					"Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT, %4$s",
					location.getLatitude(), location.getLongitude(), new Date(
							location.getTime()), location.getProvider());
			try {
				FileOutputStream fileOutputStream = openFileOutput("location.txt",MODE_APPEND); //mode_append
				fileOutputStream.write(str.getBytes());
				String n = "\n";
				fileOutputStream.write(n.getBytes());
				fileOutputStream.close();
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch (IOException e){
				e.printStackTrace();
			}
			return str;
		}
	}

	public void read(View view){
		try {
			FileInputStream fileInputStream = openFileInput("location.txt");
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			StringBuffer stringBuffer = new StringBuffer();
			String lines;
			while((lines= bufferedReader.readLine())!=null){
				stringBuffer.append(lines + "\n");
				tvFile.setText(stringBuffer.toString());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	private void checkEnabled() {//проверка включенности провайдеров и отображение инфы
		tvEnabledGPS.setText("Enabled: "
				+ locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER));
		tvEnabledNet.setText("Enabled: "
				+ locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
	}

	public void onClickLocationSettings(View view) {//при нажатии на кнопку открываются настройки
		startActivity(new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	};
	//в манифесте прописываем разрешение на определение координат

}