package com.example.paymentgateway.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/mock-payment")
@CrossOrigin("*")
public class MockPaymentController {

    @GetMapping(value = "/{gatewayTransactionRef}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> viewMockPayment(@PathVariable String gatewayTransactionRef) {
        String html = """
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Thanh Toán - Gateway</title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:'Inter',sans-serif;min-height:100vh;background:linear-gradient(135deg,#0f0c29,#302b63,#24243e);display:flex;align-items:center;justify-content:center;padding:20px}
.card{background:rgba(255,255,255,0.06);backdrop-filter:blur(20px);border:1px solid rgba(255,255,255,0.1);border-radius:24px;padding:48px 40px;max-width:480px;width:100%;color:#fff;box-shadow:0 25px 50px rgba(0,0,0,0.4)}
.logo{text-align:center;margin-bottom:32px}
.logo-icon{width:64px;height:64px;background:linear-gradient(135deg,#667eea,#764ba2);border-radius:16px;display:inline-flex;align-items:center;justify-content:center;font-size:28px;margin-bottom:12px}
.logo h1{font-size:20px;font-weight:700;letter-spacing:-0.5px}
.logo p{font-size:13px;color:rgba(255,255,255,0.5);margin-top:4px}
.info{background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.08);border-radius:16px;padding:24px;margin-bottom:28px}
.info-row{display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid rgba(255,255,255,0.06)}
.info-row:last-child{border-bottom:none}
.info-row .label{font-size:13px;color:rgba(255,255,255,0.5)}
.info-row .value{font-size:14px;font-weight:600;color:#e0e7ff}
.amount-row .value{font-size:22px;color:#a5b4fc;font-weight:700}
.status-badge{display:inline-flex;align-items:center;gap:6px;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600}
.status-pending{background:rgba(251,191,36,0.15);color:#fbbf24}
.status-success{background:rgba(52,211,153,0.15);color:#34d399}
.btn-group{display:flex;gap:12px;margin-top:8px}
.btn{flex:1;padding:14px;border:none;border-radius:14px;font-size:15px;font-weight:600;cursor:pointer;transition:all .3s;font-family:'Inter',sans-serif}
.btn-confirm{background:linear-gradient(135deg,#667eea,#764ba2);color:#fff}
.btn-confirm:hover{transform:translateY(-2px);box-shadow:0 8px 25px rgba(102,126,234,0.4)}
.btn-cancel{background:rgba(255,255,255,0.08);color:rgba(255,255,255,0.7);border:1px solid rgba(255,255,255,0.1)}
.btn-cancel:hover{background:rgba(239,68,68,0.15);color:#ef4444;border-color:rgba(239,68,68,0.3)}
.result{text-align:center;padding:32px 0}
.result-icon{font-size:56px;margin-bottom:16px}
.result h2{font-size:20px;margin-bottom:8px}
.result p{font-size:14px;color:rgba(255,255,255,0.5)}
.secure-badge{text-align:center;margin-top:24px;font-size:12px;color:rgba(255,255,255,0.3);display:flex;align-items:center;justify-content:center;gap:6px}
.hidden{display:none}
@keyframes fadeIn{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:translateY(0)}}
.fade-in{animation:fadeIn .5s ease}
</style>
</head>
<body>
<div class="card fade-in">
  <div class="logo">
    <div class="logo-icon">🔒</div>
    <h1>Payment Gateway</h1>
    <p>Cổng thanh toán bảo mật</p>
  </div>
  <div id="paymentForm">
    <div class="info">
      <div class="info-row"><span class="label">Mã giao dịch</span><span class="value" style="font-size:12px;word-break:break-all">""" + gatewayTransactionRef + """
</span></div>
      <div class="info-row"><span class="label">Trạng thái</span><span class="value"><span class="status-badge status-pending">⏳ Chờ thanh toán</span></span></div>
      <div class="info-row amount-row"><span class="label">Tổng tiền</span><span class="value">Demo Amount</span></div>
    </div>
    <div class="btn-group">
      <button class="btn btn-cancel" onclick="showResult(false)">✕ Huỷ bỏ</button>
      <button class="btn btn-confirm" onclick="showResult(true)">✓ Xác nhận</button>
    </div>
  </div>
  <div id="paymentResult" class="hidden"></div>
  <div class="secure-badge">🔐 Kết nối được mã hoá SSL/TLS</div>
</div>
<script>
function showResult(success){
  document.getElementById('paymentForm').classList.add('hidden');
  var r=document.getElementById('paymentResult');
  r.classList.remove('hidden');
  if(success){
    r.innerHTML='<div class="result fade-in"><div class="result-icon">✅</div><h2>Thanh toán thành công!</h2><p>Giao dịch đã được xử lý. Gateway đã gửi callback (IPN) về LMS.</p><p style="margin-top:12px;color:#a5b4fc;font-size:12px">Transaction: """ + gatewayTransactionRef + """
</p></div>';
  } else {
    r.innerHTML='<div class="result fade-in"><div class="result-icon">❌</div><h2>Đã huỷ thanh toán</h2><p>Bạn đã huỷ giao dịch này.</p></div>';
  }
}
</script>
</body>
</html>
""";
        return ResponseEntity.ok(html);
    }
}
