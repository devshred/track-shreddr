package org.devshred.tracks.utils

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader

class SheetReader

fun readEtappenplan(spreadsheetId: String): List<List<Any>> {
    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val range = "Etappenplan!B3:Q"
    val service: Sheets = Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
        .setApplicationName(APPLICATION_NAME).build()
    val response: ValueRange = service.spreadsheets().values().get(spreadsheetId, range).execute()
    return response.getValues()
}

fun findCustomPointOfInterests(spreadsheetId: String, sheetName: String): List<List<Any>> {
    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val range = "$sheetName!B4:D"
    val service = Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
        .setApplicationName(APPLICATION_NAME).build()
    return try {
        val response = service.spreadsheets().values()[spreadsheetId, range].execute()
        response.getValues()
    } catch (e: GoogleJsonResponseException) {
        // e.g. spreadsheet doesn't exists
        emptyList()
    }
}

private fun getCredentials(httpTransport: NetHttpTransport): Credential {
    val stream = SheetReader::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
        ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(stream))
    val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build()
    val receiver = LocalServerReceiver.Builder().setPort(8888).build()
    return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
}

private const val APPLICATION_NAME = "Track Shreddr"
private const val CREDENTIALS_FILE_PATH = "/credentials.json"
private const val TOKENS_DIRECTORY_PATH = "tokens"
private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
private val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)
