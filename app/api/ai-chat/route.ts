// app/api/ai-chat/route.ts
import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
    try {
        const { messages } = await req.json();

        const apiKey = process.env.OPENAI_API_KEY;
        if (!apiKey) {
            return NextResponse.json(
                { reply: "Missing OPENAI_API_KEY on server." },
                { status: 500 }
            );
        }

        const r = await fetch("https://api.openai.com/v1/chat/completions", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${apiKey}`,
                // If you're using a project-scoped key (sk-proj-...), this header can help:
                // "OpenAI-Project": process.env.OPENAI_PROJECT_ID ?? "",
            },
            body: JSON.stringify({
                model: "gpt-4o-mini",
                temperature: 0.4,
                messages, // [{role, content}]
            }),
        });

        const text = await r.text(); // read once for easier debugging
        if (!r.ok) {
            return NextResponse.json(
                { reply: `OpenAI error (${r.status}): ${text}` },
                { status: 500 }
            );
        }

        const data = JSON.parse(text);
        const reply = data?.choices?.[0]?.message?.content ?? "No response.";
        return NextResponse.json({ reply });
    } catch (err: any) {
        console.error("AI route error:", err);
        return NextResponse.json(
            { reply: `Server error in /api/ai-chat: ${String(err?.message ?? err)}` },
            { status: 500 }
        );
    }
}
