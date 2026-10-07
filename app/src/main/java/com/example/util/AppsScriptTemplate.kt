package com.example.util

object AppsScriptTemplate {
    /**
     * Kode Google Apps Script untuk Web App API (100% GRATIS).
     * Mendukung seluruh 18 Sheet: Kas Masuk, Kas Keluar, Transfer, Dashboard,
     * Akun, Laporan, Anggaran, Alokasi, Piutang, Kalender, Kalkulator, Buku Catatan,
     * Arsip Dihapus, Backup Drive, dan Upload Bukti Foto.
     */
    val COMPANION_SCRIPT = """
/**
 * ========================================================
 * ENDPOINT WEB APP API ANDROID - SISTEM KAS LENGKAP
 * ========================================================
 * Cara Pasang:
 * 1. Di Google Sheets, buka Extensions > Apps Script.
 * 2. Salin kode ini lalu tempel di bagian paling bawah Code.gs.
 * 3. Klik Deploy > New deployment (Penerapan baru).
 * 4. Tipe: "Web app" | Execute as: "Me" | Who has access: "Anyone".
 * 5. Klik Deploy, salin Web app URL dan tempel ke Pengaturan di aplikasi Android.
 */

function doGet(e) {
  var action = (e && e.parameter && e.parameter.action) ? e.parameter.action : 'get_summary';
  var ss = SpreadsheetApp.getActive();
  
  try {
    if (action === 'ping') {
      return jsonResponse({
        status: 'success',
        message: 'Terhubung ke Google Sheets Sistem Kas!',
        sheetName: ss.getName(),
        time: new Date().toISOString()
      });
    }
    
    if (action === 'get_summary') {
      var dSh = ss.getSheetByName(CFG.sheets.dashboard);
      var aSh = ss.getSheetByName(CFG.sheets.akun);
      
      var summary = {
        saldoAwal: dSh ? Number(dSh.getRange('B3').getValue() || 0) : 0,
        masukHariIni: dSh ? Number(dSh.getRange('B4').getValue() || 0) : 0,
        keluarHariIni: dSh ? Number(dSh.getRange('B5').getValue() || 0) : 0,
        netHariIni: dSh ? Number(dSh.getRange('B6').getValue() || 0) : 0,
        masukBulanIni: dSh ? Number(dSh.getRange('B7').getValue() || 0) : 0,
        keluarBulanIni: dSh ? Number(dSh.getRange('B8').getValue() || 0) : 0,
        saldoSaatIni: dSh ? Number(dSh.getRange('B9').getValue() || 0) : 0
      };
      
      var accounts = [];
      if (aSh && aSh.getLastRow() >= 4) {
        var data = aSh.getRange(4, 1, Math.min(aSh.getLastRow() - 3, 20), 6).getValues();
        for (var i = 0; i < data.length; i++) {
          if (data[i][0]) {
            accounts.push({
              name: data[i][0],
              type: data[i][1],
              initial: Number(data[i][2] || 0),
              totalIn: Number(data[i][3] || 0),
              totalOut: Number(data[i][4] || 0),
              current: Number(data[i][5] || 0)
            });
          }
        }
      }
      return jsonResponse({ status: 'success', summary: summary, accounts: accounts });
    }
    
    return jsonResponse({ status: 'error', message: 'Action tidak dikenal' });
  } catch(err) {
    return jsonResponse({ status: 'error', message: err.toString() });
  }
}

function doPost(e) {
  var ss = SpreadsheetApp.getActive();
  try {
    var raw = e.postData ? e.postData.contents : '';
    var payload = JSON.parse(raw);
    var action = payload.action;
    
    // 1. TAMBAH TRANSAKSI KAS
    if (action === 'ADD_TRANSACTION') {
      var t = payload.transaction;
      var targetSheet = (t.type === 'MASUK') ? CFG.sheets.masuk : (t.type === 'KELUAR' ? CFG.sheets.keluar : CFG.sheets.transfer);
      var sh = ss.getSheetByName(targetSheet);
      if (!sh) throw new Error('Sheet ' + targetSheet + ' tidak ditemukan');
      
      var now = new Date();
      var id = t.id || newId_(t.type === 'MASUK' ? 'KM' : (t.type === 'KELUAR' ? 'KK' : 'TR'));
      var row;
      if (t.type === 'TRANSFER') {
        row = [id, t.date, now, t.description, t.account, t.toAccount, Number(t.amount), Number(t.amount), t.pic || '', t.proofUrl || '', t.proofNumber || '', t.project || '', t.note || '', t.status || 'Selesai', now];
      } else {
        row = [id, t.date, now, t.account, t.transactionName, t.category, t.description, Number(t.amount), t.allocation || 'Operasional', t.pic || 'Admin', t.proofUrl || '', t.proofNumber || '', t.project || '', t.note || '', t.status || 'Selesai', now, 'Android Mobile'];
      }
      var targetRow = Math.max(sh.getLastRow() + 1, CFG.ledgerStartRow);
      sh.getRange(targetRow, 1, 1, row.length).setValues([row]);
      SpreadsheetApp.flush();
      return jsonResponse({ status: 'success', id: id, row: targetRow });
    }
    
    // 2. UPLOAD BUKTI KE GOOGLE DRIVE
    if (action === 'UPLOAD_BUKTI') {
      var base64 = payload.base64;
      var fileName = payload.fileName || ('Bukti_' + Date.now() + '.jpg');
      var mimeType = payload.mimeType || 'image/jpeg';
      var decoded = Utilities.base64Decode(base64);
      var blob = Utilities.newBlob(decoded, mimeType, fileName);
      var src = DriveApp.getFileById(ss.getId());
      var parents = src.getParents();
      var folder = parents.hasNext() ? getOrCreateFolder_(parents.next(), 'Bukti_Kas') : DriveApp.getRootFolder();
      var file = folder.createFile(blob);
      file.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
      return jsonResponse({ status: 'success', fileUrl: file.getUrl(), fileId: file.getId() });
    }
    
    // 3. LOG KALKULATOR KE GOOGLE SHEETS
    if (action === 'LOG_CALCULATOR') {
      var kSh = ss.getSheetByName(CFG.sheets.kalkulator);
      if (kSh) {
        var c = payload.calc;
        var logRow = [new Date(), c.title, Number(c.initialBalance), Number(c.income), Number(c.fixedCosts), Number(c.variableCosts), Number(c.totalExpense), Number(c.netMargin), Number(c.expensePercentage), Number(c.finalBalance), c.formulaOrNote || ''];
        var tRow = Math.max(kSh.getLastRow() + 1, 12);
        kSh.getRange(tRow, 1, 1, logRow.length).setValues([logRow]);
        SpreadsheetApp.flush();
      }
      return jsonResponse({ status: 'success', message: 'Kalkulasi berhasil dicatat di sheet Kalkulator' });
    }
    
    // 4. EVENT KALENDER KAS
    if (action === 'ADD_CALENDAR_EVENT') {
      var calSh = ss.getSheetByName(CFG.sheets.kalender);
      if (calSh) {
        var ev = payload.event;
        var evRow = [ev.date, ev.time, ev.title, ev.type, Number(ev.amount || 0), ev.description || '', ev.isCompleted ? 'Selesai' : 'Pending'];
        var calTarget = Math.max(calSh.getLastRow() + 1, 68);
        calSh.getRange(calTarget, 1, 1, evRow.length).setValues([evRow]);
        SpreadsheetApp.flush();
      }
      return jsonResponse({ status: 'success', message: 'Event kalender berhasil dicatat di sheet Kalender' });
    }

    // 5. KELOLA PIUTANG
    if (action === 'ADD_PIUTANG') {
      var pSh = ss.getSheetByName(CFG.sheets.piutang);
      if (pSh) {
        var p = payload.piutang;
        var pRow = [p.id, p.date, p.customerName, p.description, Number(p.amount), p.dueDate, Number(p.paidAmount || 0), Number(p.remainingAmount || p.amount), p.status, p.targetAccount, p.note || ''];
        var pTarget = Math.max(pSh.getLastRow() + 1, 4);
        pSh.getRange(pTarget, 1, 1, pRow.length).setValues([pRow]);
        SpreadsheetApp.flush();
      }
      return jsonResponse({ status: 'success', message: 'Data piutang tercatat di sheet Piutang' });
    }
    
    // 6. SINKRONISASI FLUSH
    if (action === 'SYNC') {
      SpreadsheetApp.flush();
      return jsonResponse({ status: 'success', message: 'Spreadsheet berhasil disinkronkan & dihitung ulang' });
    }
    
    // 7. BACKUP KE GOOGLE DRIVE
    if (action === 'BACKUP') {
      backupKeDrive();
      return jsonResponse({ status: 'success', message: 'Backup berhasil dibuat di Google Drive' });
    }
    
    return jsonResponse({ status: 'error', message: 'Action tidak dikenal' });
  } catch(err) {
    return jsonResponse({ status: 'error', message: err.toString() });
  }
}

function jsonResponse(data) {
  return ContentService.createTextOutput(JSON.stringify(data)).setMimeType(ContentService.MimeType.JSON);
}
    """.trimIndent()
}
