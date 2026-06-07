import sys
import os
import base64
from PySide6.QtWidgets import (QApplication, QMainWindow, QWidget, QVBoxLayout, 
                             QHBoxLayout, QLabel, QTextEdit, QPushButton, 
                             QTabWidget, QFileDialog, QMessageBox, QRadioButton, 
                             QButtonGroup, QFrame)
from PySide6.QtGui import QFont, QClipboard, QIcon
from PySide6.QtCore import Qt

# Cryptography imports
from Cryptodome.PublicKey import RSA
from Cryptodome.Signature import pkcs1_15
from Cryptodome.Hash import SHA256

class MockHash:
    def __init__(self, digest_bytes):
        self.digest_bytes = digest_bytes
        self.oid = "2.16.840.1.101.3.4.2.1"  # SHA-256 OID
    def digest(self):
        return self.digest_bytes

class SignatureTool(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("The Grindery - Offline Signature Tool")
        self.resize(750, 600)
        self.init_ui()
        self.apply_style()

    def init_ui(self):
        # Central widget and layout
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        main_layout = QVBoxLayout(central_widget)
        main_layout.setContentsMargins(15, 15, 15, 15)
        main_layout.setSpacing(10)

        # Header Title
        header_label = QLabel("OFFLINE DIGITAL SIGNATURE TOOL")
        header_label.setObjectName("headerLabel")
        header_label.setAlignment(Qt.AlignCenter)
        main_layout.addWidget(header_label)

        # Subtitle
        subtitle_label = QLabel("Công cụ tạo khóa và ký số ngoại tuyến bảo mật của The Grindery")
        subtitle_label.setObjectName("subtitleLabel")
        subtitle_label.setAlignment(Qt.AlignCenter)
        main_layout.addWidget(subtitle_label)

        # Tab Widget
        self.tabs = QTabWidget()
        self.tabs.setObjectName("mainTabs")
        main_layout.addWidget(self.tabs)

        # Add tabs
        self.create_key_gen_tab()
        self.create_sign_tab()
        self.create_verify_tab()

    def create_key_gen_tab(self):
        tab = QWidget()
        layout = QVBoxLayout(tab)
        layout.setContentsMargins(15, 15, 15, 15)
        layout.setSpacing(12)

        info_label = QLabel("Tạo cặp khóa mật mã RSA 2048-bit mới. Khóa công khai dùng để cấu hình trên web, khóa bí mật lưu giữ riêng dưới máy.")
        info_label.setWordWrap(True)
        info_label.setObjectName("infoLabel")
        layout.addWidget(info_label)

        # Output areas for keys
        keys_layout = QHBoxLayout()
        
        # Public Key Area
        pub_layout = QVBoxLayout()
        pub_label = QLabel("PUBLIC KEY (Khóa công khai - PEM):")
        pub_label.setObjectName("fieldLabel")
        self.pub_txt = QTextEdit()
        self.pub_txt.setReadOnly(True)
        self.pub_txt.setFont(QFont("Courier New", 10))
        pub_btn_layout = QHBoxLayout()
        pub_copy_btn = QPushButton("Copy")
        pub_copy_btn.clicked.connect(lambda: self.copy_to_clipboard(self.pub_txt.toPlainText()))
        pub_save_btn = QPushButton("Lưu file...")
        pub_save_btn.clicked.connect(lambda: self.save_key_file("public_key.pem", self.pub_txt.toPlainText()))
        pub_btn_layout.addWidget(pub_copy_btn)
        pub_btn_layout.addWidget(pub_save_btn)
        pub_layout.addWidget(pub_label)
        pub_layout.addWidget(self.pub_txt)
        pub_layout.addLayout(pub_btn_layout)

        # Private Key Area
        priv_layout = QVBoxLayout()
        priv_label = QLabel("PRIVATE KEY (Khóa bí mật - PEM):")
        priv_label.setObjectName("fieldLabel")
        self.priv_txt = QTextEdit()
        self.priv_txt.setReadOnly(True)
        self.priv_txt.setFont(QFont("Courier New", 10))
        priv_btn_layout = QHBoxLayout()
        priv_copy_btn = QPushButton("Copy")
        priv_copy_btn.clicked.connect(lambda: self.copy_to_clipboard(self.priv_txt.toPlainText()))
        priv_save_btn = QPushButton("Lưu file...")
        priv_save_btn.clicked.connect(lambda: self.save_key_file("private_key.pem", self.priv_txt.toPlainText()))
        priv_btn_layout.addWidget(priv_copy_btn)
        priv_btn_layout.addWidget(priv_save_btn)
        priv_layout.addWidget(priv_label)
        priv_layout.addWidget(self.priv_txt)
        priv_layout.addLayout(priv_btn_layout)

        keys_layout.addLayout(pub_layout)
        keys_layout.addLayout(priv_layout)
        layout.addLayout(keys_layout)

        # Generate Button
        self.gen_btn = QPushButton("TẠO CẶP KHÓA MỚI (GENERATE KEY PAIR)")
        self.gen_btn.setObjectName("primaryButton")
        self.gen_btn.setMinimumHeight(45)
        self.gen_btn.clicked.connect(self.generate_keys)
        layout.addWidget(self.gen_btn)

        self.tabs.addTab(tab, "Tạo Khóa")

    def create_sign_tab(self):
        tab = QWidget()
        layout = QVBoxLayout(tab)
        layout.setContentsMargins(15, 15, 15, 15)
        layout.setSpacing(12)

        # Input Data Area
        data_label = QLabel("Dữ liệu cần ký (Data to Sign):")
        data_label.setObjectName("fieldLabel")
        self.sign_data_txt = QTextEdit()
        self.sign_data_txt.setPlaceholderText("Dán chuỗi dữ liệu gốc của đơn hàng (Raw Order Data) hoặc bất kỳ nội dung nào cần ký...")
        layout.addWidget(data_label)
        layout.addWidget(self.sign_data_txt)

        # Mode Selection
        mode_layout = QHBoxLayout()
        mode_label = QLabel("Loại dữ liệu:")
        mode_label.setObjectName("fieldLabel")
        self.btn_group = QButtonGroup()
        
        self.rad_raw_order = QRadioButton("Chuỗi Đơn Hàng Gốc (Raw Order)")
        self.rad_raw_order.setChecked(True)
        self.rad_hash = QRadioButton("Mã Băm Đơn Hàng (Order Hash - Base64)")
        self.rad_text = QRadioButton("Văn Bản Thường (Plain Text)")
        
        self.btn_group.addButton(self.rad_raw_order)
        self.btn_group.addButton(self.rad_hash)
        self.btn_group.addButton(self.rad_text)
        
        mode_layout.addWidget(mode_label)
        mode_layout.addWidget(self.rad_raw_order)
        mode_layout.addWidget(self.rad_hash)
        mode_layout.addWidget(self.rad_text)
        mode_layout.addStretch()
        layout.addLayout(mode_layout)

        # Private Key Input Area
        key_layout = QVBoxLayout()
        key_header_layout = QHBoxLayout()
        key_label = QLabel("Khóa bí mật dùng để ký (Private Key PEM):")
        key_label.setObjectName("fieldLabel")
        load_key_btn = QPushButton("Chọn file Private Key...")
        load_key_btn.setObjectName("secondaryButton")
        load_key_btn.clicked.connect(self.load_private_key_file)
        key_header_layout.addWidget(key_label)
        key_header_layout.addWidget(load_key_btn)
        key_header_layout.addStretch()
        
        self.sign_key_txt = QTextEdit()
        self.sign_key_txt.setPlaceholderText("-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----")
        self.sign_key_txt.setFont(QFont("Courier New", 9))
        self.sign_key_txt.setMaximumHeight(100)
        key_layout.addLayout(key_header_layout)
        key_layout.addWidget(self.sign_key_txt)
        layout.addLayout(key_layout)

        # Sign Action Button
        self.sign_btn = QPushButton("KÝ SỐ (GENERATE SIGNATURE)")
        self.sign_btn.setObjectName("primaryButton")
        self.sign_btn.setMinimumHeight(45)
        self.sign_btn.clicked.connect(self.sign_data)
        layout.addWidget(self.sign_btn)

        # Output Signature Area
        sig_label = QLabel("Chữ ký kết quả (Signature - Base64):")
        sig_label.setObjectName("fieldLabel")
        self.sig_txt = QTextEdit()
        self.sig_txt.setReadOnly(True)
        self.sig_txt.setFont(QFont("Courier New", 9))
        self.sig_txt.setMaximumHeight(80)
        
        sig_btn_layout = QHBoxLayout()
        sig_copy_btn = QPushButton("Copy Chữ Ký")
        sig_copy_btn.setObjectName("primaryButton")
        sig_copy_btn.clicked.connect(lambda: self.copy_to_clipboard(self.sig_txt.toPlainText()))
        sig_btn_layout.addWidget(sig_copy_btn)
        sig_btn_layout.addStretch()

        layout.addWidget(sig_label)
        layout.addWidget(self.sig_txt)
        layout.addLayout(sig_btn_layout)

        self.tabs.addTab(tab, "Ký Số")

    def create_verify_tab(self):
        tab = QWidget()
        layout = QVBoxLayout(tab)
        layout.setContentsMargins(15, 15, 15, 15)
        layout.setSpacing(12)

        # Input Data
        data_label = QLabel("Dữ liệu nguồn gốc:")
        data_label.setObjectName("fieldLabel")
        self.verify_data_txt = QTextEdit()
        self.verify_data_txt.setPlaceholderText("Nhập chuỗi dữ liệu gốc giống lúc ký...")
        layout.addWidget(data_label)
        layout.addWidget(self.verify_data_txt)

        # Public Key
        key_header_layout = QHBoxLayout()
        key_label = QLabel("Khóa công khai để xác thực (Public Key PEM):")
        key_label.setObjectName("fieldLabel")
        load_pub_btn = QPushButton("Chọn file Public Key...")
        load_pub_btn.setObjectName("secondaryButton")
        load_pub_btn.clicked.connect(self.load_public_key_file)
        key_header_layout.addWidget(key_label)
        key_header_layout.addWidget(load_pub_btn)
        key_header_layout.addStretch()

        self.verify_key_txt = QTextEdit()
        self.verify_key_txt.setPlaceholderText("-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----")
        self.verify_key_txt.setFont(QFont("Courier New", 9))
        self.verify_key_txt.setMaximumHeight(100)
        layout.addLayout(key_header_layout)
        layout.addWidget(self.verify_key_txt)

        # Signature to verify
        sig_label = QLabel("Chữ ký số cần xác thực (Base64):")
        sig_label.setObjectName("fieldLabel")
        self.verify_sig_txt = QTextEdit()
        self.verify_sig_txt.setPlaceholderText("Dán mã chữ ký Base64 cần kiểm tra...")
        self.verify_sig_txt.setMaximumHeight(80)
        layout.addWidget(sig_label)
        layout.addWidget(self.verify_sig_txt)

        # Verify Button
        self.verify_btn = QPushButton("XÁC THỰC CHỮ KÝ (VERIFY SIGNATURE)")
        self.verify_btn.setObjectName("primaryButton")
        self.verify_btn.setMinimumHeight(45)
        self.verify_btn.clicked.connect(self.verify_signature)
        layout.addWidget(self.verify_btn)

        self.tabs.addTab(tab, "Xác Thực")

    # Business Logic Functions
    def generate_keys(self):
        try:
            key = RSA.generate(2048)
            private_pem = key.export_key(format='PEM', pkcs=8).decode('utf-8')
            public_pem = key.publickey().export_key(format='PEM').decode('utf-8')
            
            self.pub_txt.setPlainText(public_pem)
            self.priv_txt.setPlainText(private_pem)
            QMessageBox.information(self, "Thành Công", "Đã tạo cặp khóa RSA 2048-bit thành công!")
        except Exception as e:
            QMessageBox.critical(self, "Lỗi", f"Không thể tạo cặp khóa: {str(e)}")

    def save_key_file(self, default_name, content):
        if not content:
            QMessageBox.warning(self, "Cảnh Báo", "Không có nội dung để lưu!")
            return
        file_path, _ = QFileDialog.getSaveFileName(self, "Lưu File Khóa", default_name, "PEM Files (*.pem);;All Files (*)")
        if file_path:
            try:
                with open(file_path, "w", encoding="utf-8") as f:
                    f.write(content)
                QMessageBox.information(self, "Thành Công", "Đã lưu khóa thành công!")
            except Exception as e:
                QMessageBox.critical(self, "Lỗi", f"Không thể lưu file: {str(e)}")

    def load_private_key_file(self):
        file_path, _ = QFileDialog.getOpenFileName(self, "Chọn file Private Key", "", "PEM Files (*.pem);;All Files (*)")
        if file_path:
            try:
                with open(file_path, "r", encoding="utf-8") as f:
                    self.sign_key_txt.setPlainText(f.read())
            except Exception as e:
                QMessageBox.critical(self, "Lỗi", f"Không thể mở file: {str(e)}")

    def load_public_key_file(self):
        file_path, _ = QFileDialog.getOpenFileName(self, "Chọn file Public Key", "", "PEM Files (*.pem);;All Files (*)")
        if file_path:
            try:
                with open(file_path, "r", encoding="utf-8") as f:
                    self.verify_key_txt.setPlainText(f.read())
            except Exception as e:
                QMessageBox.critical(self, "Lỗi", f"Không thể mở file: {str(e)}")

    def copy_to_clipboard(self, text):
        if text:
            clipboard = QApplication.clipboard()
            clipboard.setText(text)
            QMessageBox.information(self, "Đã Copy", "Đã copy nội dung vào clipboard!")
        else:
            QMessageBox.warning(self, "Cảnh Báo", "Không có nội dung để copy!")

    def sign_data(self):
        data = self.sign_data_txt.toPlainText()
        key_pem = self.sign_key_txt.toPlainText()

        if not data:
            QMessageBox.warning(self, "Cảnh Báo", "Vui lòng nhập dữ liệu cần ký!")
            return
        if not key_pem:
            QMessageBox.warning(self, "Cảnh Báo", "Vui lòng dán khóa bí mật (Private Key)!")
            return

        try:
            # Load private key
            private_key = RSA.import_key(key_pem)
            
            # Compute hash bytes based on type
            if self.rad_raw_order.isChecked():
                # Hash raw order text with SHA-256 (giving 32 bytes)
                h1 = SHA256.new(data.encode('utf-8')).digest()
                # Hash again because Java does signature.update(H1_bytes), which hashes it again inside SHA256withRSA
                h2 = SHA256.new(h1).digest()
                hash_to_sign = MockHash(h2)
            elif self.rad_hash.isChecked():
                # Data is already the Base64 representation of H1
                h1 = base64.b64decode(data.strip())
                h2 = SHA256.new(h1).digest()
                hash_to_sign = MockHash(h2)
            else:
                # Plain text mode: normal SHA256 hash and sign
                h = SHA256.new(data.encode('utf-8'))
                hash_to_sign = h

            # Sign using PKCS1 v1.5
            signature_bytes = pkcs1_15.new(private_key).sign(hash_to_sign)
            signature_b64 = base64.b64encode(signature_bytes).decode('utf-8')
            
            self.sig_txt.setPlainText(signature_b64)
        except Exception as e:
            QMessageBox.critical(self, "Lỗi Ký Số", f"Ký số thất bại: {str(e)}")

    def verify_signature(self):
        data = self.verify_data_txt.toPlainText()
        key_pem = self.verify_key_txt.toPlainText()
        sig_b64 = self.verify_sig_txt.toPlainText().strip()

        if not data or not key_pem or not sig_b64:
            QMessageBox.warning(self, "Cảnh Báo", "Vui lòng điền đầy đủ dữ liệu nguồn, khóa công khai và chữ ký!")
            return

        try:
            public_key = RSA.import_key(key_pem)
            sig_bytes = base64.b64decode(sig_b64)
            
            # Determine hashing method (assume raw order mode or normal text mode)
            # Try raw order double hashing first, if fails try normal text hashing
            h1 = SHA256.new(data.encode('utf-8')).digest()
            h2 = SHA256.new(h1).digest()
            hash_double = MockHash(h2)
            
            hash_single = SHA256.new(data.encode('utf-8'))

            verified = False
            try:
                pkcs1_15.new(public_key).verify(hash_double, sig_bytes)
                verified = True
            except (ValueError, TypeError):
                try:
                    pkcs1_15.new(public_key).verify(hash_single, sig_bytes)
                    verified = True
                except (ValueError, TypeError):
                    pass

            if verified:
                QMessageBox.information(self, "Kết Quả", "Chữ ký hợp lệ! (Signature VALID)")
            else:
                QMessageBox.critical(self, "Kết Quả", "Chữ ký không hợp lệ! (Signature INVALID)")
        except Exception as e:
            QMessageBox.critical(self, "Lỗi", f"Lỗi xác thực: {str(e)}")

    def apply_style(self):
        # Premium dark mode stylesheet
        self.setStyleSheet("""
            QMainWindow {
                background-color: #0f172a;
            }
            #headerLabel {
                color: #e2e8f0;
                font-family: 'Inter', 'Segoe UI', Arial;
                font-size: 20px;
                font-weight: bold;
                margin-top: 10px;
            }
            #subtitleLabel {
                color: #94a3b8;
                font-family: 'Inter', 'Segoe UI', Arial;
                font-size: 13px;
                margin-bottom: 10px;
            }
            QTabWidget::pane {
                border: 1px solid #1e293b;
                background: #1e293b;
                border-radius: 8px;
            }
            QTabBar::tab {
                background: #0f172a;
                color: #64748b;
                border: 1px solid #1e293b;
                border-bottom: none;
                padding: 10px 20px;
                font-family: 'Inter', 'Segoe UI', Arial;
                font-weight: 500;
                font-size: 14px;
                border-top-left-radius: 6px;
                border-top-right-radius: 6px;
            }
            QTabBar::tab:selected {
                background: #1e293b;
                color: #38bdf8;
                border-color: #1e293b;
            }
            #infoLabel {
                color: #cbd5e1;
                font-family: 'Inter', 'Segoe UI', Arial;
                font-size: 13px;
                line-height: 1.4;
            }
            #fieldLabel {
                color: #f1f5f9;
                font-family: 'Inter', 'Segoe UI', Arial;
                font-size: 13px;
                font-weight: 600;
            }
            QTextEdit {
                background-color: #0f172a;
                color: #e2e8f0;
                border: 1px solid #334155;
                border-radius: 6px;
                padding: 8px;
            }
            QTextEdit:focus {
                border: 1px solid #38bdf8;
            }
            QPushButton {
                background-color: #0f172a;
                color: #e2e8f0;
                border: 1px solid #334155;
                border-radius: 6px;
                padding: 8px 16px;
                font-family: 'Inter', 'Segoe UI', Arial;
                font-weight: 500;
            }
            QPushButton:hover {
                background-color: #1e293b;
                border-color: #475569;
            }
            #primaryButton {
                background-color: #0284c7;
                color: #ffffff;
                border: none;
                font-weight: bold;
            }
            #primaryButton:hover {
                background-color: #0369a1;
            }
            #secondaryButton {
                background-color: #334155;
                color: #f1f5f9;
                border: none;
            }
            #secondaryButton:hover {
                background-color: #475569;
            }
            QRadioButton {
                color: #cbd5e1;
                font-family: 'Inter', 'Segoe UI', Arial;
                font-size: 12px;
            }
        """)

if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = SignatureTool()
    window.show()
    sys.exit(app.exec())
