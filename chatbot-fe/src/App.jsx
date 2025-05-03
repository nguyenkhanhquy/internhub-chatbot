import React, { useState } from "react";
import Chat from "./components/Chat";
import ChatButton from "./components/ChatButton";

function App() {
    const [isChatOpen, setIsChatOpen] = useState(false);

    const toggleChat = () => {
        setIsChatOpen((prevState) => !prevState);
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-blue-100">
            <header className="text-center py-8 max-w-6xl mx-auto w-[95%]">
                <h1 className="text-3xl font-bold text-indigo-700 mb-2 drop-shadow-sm">AI Chatbot</h1>
                <p className="text-gray-600">Trợ lý thông minh giúp bạn trả lời mọi câu hỏi</p>
            </header>

            {/* Main content */}
            <div className="max-w-6xl mx-auto w-[95%] p-5">
                <div className="bg-white p-6 rounded-xl shadow-md">
                    <h2 className="text-xl font-semibold text-gray-800 mb-4">Chào mừng bạn đến với AI Chatbot</h2>
                    <p className="text-gray-600">
                        Đây là trang demo của ứng dụng AI Chatbot. Hệ thống được phát triển dựa trên Spring Boot và
                        React để tạo một trợ lý AI thông minh.
                    </p>
                    <p className="text-gray-600 mt-4">
                        Nhấn vào nút hộp thoại góc phải bên dưới để bắt đầu trò chuyện với AI.
                    </p>
                </div>
            </div>

            {/* Chat components */}
            <ChatButton onClick={toggleChat} />
            <Chat isOpen={isChatOpen} onClose={toggleChat} />
        </div>
    );
}

export default App;
