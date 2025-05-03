import React from "react";

const LoadingAnimation = () => {
    return (
        <div className="flex justify-start mb-3">
            <div className="p-3 rounded-2xl shadow-md bg-white border border-gray-200 max-w-[85%]">
                <div className="flex items-center space-x-2">
                    <span className="w-2 h-2 bg-indigo-400 rounded-full animate-bounce"></span>
                    <span className="w-2 h-2 bg-indigo-400 rounded-full animate-bounce [animation-delay:150ms]"></span>
                    <span className="w-2 h-2 bg-indigo-400 rounded-full animate-bounce [animation-delay:300ms]"></span>
                </div>
            </div>
        </div>
    );
};

export default LoadingAnimation;
