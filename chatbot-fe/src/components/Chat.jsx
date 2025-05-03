import React, { useState, useRef, useEffect } from "react";
import axios from "axios";
import LoadingAnimation from "./LoadingAnimation";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

const Chat = ({ isOpen, onClose }) => {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [isResetting, setIsResetting] = useState(false);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages, isOpen]);

    const getCurrentTime = () => {
        const now = new Date();
        return now.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!input.trim()) return;

        const userMessage = input;
        const currentTime = getCurrentTime();
        setInput("");
        setMessages((prev) => [...prev, { text: userMessage, isUser: true, time: currentTime }]);
        setIsLoading(true);

        try {
            const response = await fetch("http://localhost:8080/inference", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    query: userMessage,
                    sessionId: "user-session",
                    history: "",
                }),
            });

            const reader = response.body.getReader();
            const decoder = new TextDecoder("utf-8");
            let aiResponse = "";

            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                const chunk = decoder.decode(value, { stream: true });
                aiResponse += chunk;

                setMessages((prevMessages) => {
                    const last = prevMessages[prevMessages.length - 1];
                    if (last && !last.isUser) {
                        return prevMessages.map((msg, i) =>
                            i === prevMessages.length - 1 ? { ...msg, text: aiResponse } : msg
                        );
                    } else {
                        return [...prevMessages, { text: chunk, isUser: false, time: getCurrentTime() }];
                    }
                });
            }
        } catch (error) {
            console.error("Error:", error);
            setMessages((prev) => [
                ...prev,
                {
                    text: "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.",
                    isUser: false,
                    time: getCurrentTime(),
                },
            ]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleReset = async () => {
        if (isResetting) return;

        setIsResetting(true);
        try {
            const sessionId = "user-session";
            await axios.post(`http://localhost:8080/reset/${sessionId}`);

            setMessages([]);
        } catch (error) {
            console.error("Reset error:", error);
            setMessages([
                {
                    text: "Có lỗi xảy ra khi làm mới cuộc trò chuyện. Vui lòng thử lại.",
                    isUser: false,
                    time: getCurrentTime(),
                },
            ]);
        } finally {
            setIsResetting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed bottom-24 right-4 w-[400px] sm:w-[600px] h-[600px] bg-white rounded-xl shadow-2xl flex flex-col border border-gray-200 z-40 animate-slide-up">
            <div className="flex items-center justify-between p-3 border-b border-gray-200 bg-gradient-to-r from-indigo-600 to-blue-500 text-white rounded-t-xl">
                <div className="flex items-center">
                    <h2 className="font-semibold text-lg">AI Assistant</h2>
                </div>
                <button onClick={onClose} className="text-white hover:text-gray-200 transition-colors">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-6 w-6"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                    >
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>

            <div className="flex-1 overflow-y-auto p-4 bg-gray-50 scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-50">
                {messages.length === 0 && (
                    <div className="text-center text-gray-500 mt-4">
                        <p className="font-medium">Chào bạn!</p>
                        <p className="text-sm mt-2">Tôi là AI Assistant. Hãy đặt câu hỏi để bắt đầu cuộc trò chuyện.</p>
                    </div>
                )}
                {messages.map((message, index) => (
                    <div key={index} className={`flex ${message.isUser ? "justify-end" : "justify-start"} mb-3`}>
                        <div
                            className={`p-3 rounded-2xl shadow-sm max-w-[85%] ${
                                message.isUser
                                    ? "bg-gradient-to-r from-indigo-600 to-blue-500 text-white"
                                    : "bg-white text-gray-800 border border-gray-200"
                            }`}
                        >
                            <div className={`prose prose-sm ${message.isUser ? "prose-invert" : ""} text-left`}>
                                <ReactMarkdown remarkPlugins={[remarkGfm]}>{message.text}</ReactMarkdown>
                            </div>
                            <div
                                className={`text-[10px] mt-1 ${
                                    message.isUser ? "text-right text-blue-100" : "text-left text-gray-500"
                                }`}
                            >
                                {message.time}
                            </div>
                        </div>
                    </div>
                ))}
                {isLoading && messages[messages.length - 1]?.isUser && <LoadingAnimation />}
                <div ref={messagesEndRef} />
            </div>

            <form onSubmit={handleSubmit} className="border-t border-gray-200 p-3 bg-white rounded-b-xl">
                <div className="flex gap-2 items-center">
                    <button
                        type="button"
                        onClick={handleReset}
                        disabled={isResetting || isLoading}
                        className="p-2 bg-gradient-to-r from-purple-600 to-pink-500 text-white rounded-full hover:from-purple-700 hover:to-pink-600 transition-colors disabled:opacity-70 disabled:bg-gray-300"
                        title="Làm mới cuộc trò chuyện"
                    >
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-5 w-5"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                        >
                            <path
                                fillRule="evenodd"
                                d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
                                clipRule="evenodd"
                            />
                        </svg>
                    </button>
                    <input
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        placeholder="Nhập tin nhắn của bạn..."
                        disabled={isLoading}
                        className="flex-1 px-4 py-2 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50 disabled:bg-gray-100 disabled:cursor-not-allowed text-sm"
                    />
                    <button
                        type="submit"
                        disabled={isLoading}
                        className="p-2 bg-gradient-to-r from-indigo-600 to-blue-500 text-white rounded-full hover:from-indigo-700 hover:to-blue-600 transition-colors disabled:opacity-70 disabled:bg-gray-300"
                    >
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-5 w-5"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                        >
                            <path
                                fillRule="evenodd"
                                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-8.707l-3-3a1 1 0 00-1.414 0l-3 3a1 1 0 001.414 1.414L9 9.414V13a1 1 0 102 0V9.414l1.293 1.293a1 1 0 001.414-1.414z"
                                clipRule="evenodd"
                            />
                        </svg>
                    </button>
                </div>
            </form>
        </div>
    );
};

export default Chat;
