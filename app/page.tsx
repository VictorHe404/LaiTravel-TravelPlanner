"use client";
import Link from "next/link";
import React from "react";
import Image from "next/image"; // Import the Image component

export default function HomePage() {
    return (
        <main className="flex flex-col items-center justify-center h-screen text-center">
            {/* Add your logo here - replace with your actual logo path */}
            <div className="mb-6">
                <Image
                    src="/image/logo1.png" // Path to your logo image
                    alt="Travel Planner Logo"
                    width={200} // Adjust width as needed
                    height={200} // Adjust height as needed
                    className="mx-auto" // Center the logo
                />
            </div>

            <h1 className="text-3xl font-bold mb-4">
                Travel Planner · Explore the City Smarter
            </h1>
            <p className="text-lg text-gray-600 mb-6">
                Tell us how long you're staying and where you want to go — we'll craft
                the perfect route for each day.
            </p>

            <Link href="/planner">
                <button className="px-6 py-3 bg-blue-600 text-white rounded hover:bg-blue-700 transition">
                    Start Planning Your Trip
                </button>
            </Link>
        </main>
    );
}