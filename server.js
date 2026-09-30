import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import OpenAI from "openai";

dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

app.use(cors({ origin: "*" }));


app.use(express.json({ limit: "15mb" }));
app.get("/test", (req,res) =>
  res.send("TEST PAGE WORKS");
});
app.get("/", (req, res) => {
  res.json({
    ok: true,
    service: "AI Kitchen Scale server",
    status: "running"
  });
});

app.get("/health", (req, res) => {
  res.json({ ok: true });
});

app.post("/analyze", async (req, res) => {
  try {
    if (!process.env.OPENAI_API_KEY) {
      return res.status(500).json({
        ok: false,
        error: "OPENAI_API_KEY is not configured on the server."
      });
    }

    const { imageBase64, mimeType = "image/jpeg", language = "mk" } = req.body || {};

    if (!imageBase64 || typeof imageBase64 !== "string") {
      return res.status(400).json({
        ok: false,
        error: "imageBase64 is required."
      });
    }

    const cleanedBase64 = imageBase64.replace(/^data:image\/[a-zA-Z0-9.+-]+;base64,/, "");

    const client = new OpenAI({
      apiKey: process.env.OPENAI_API_KEY
    });

    const prompt = `
You are the food-analysis engine for an AI Kitchen Scale app.

Analyze the food visible in the image and return ONLY valid JSON.
Do not wrap the JSON in markdown.

The app language is: ${language}.

Return this exact structure:
{
  "food_name": "short food name",
  "estimated_weight_g": 0,
  "estimated_calories_kcal": 0,
  "protein_g": 0,
  "carbs_g": 0,
  "fat_g": 0,
  "confidence": 0,
  "notes": "short note"
}

Rules:
- Estimate only from what is visible.
- Weight is an estimate, not a real physical measurement.
- confidence must be an integer from 0 to 100.
- All nutrition values must be numeric.
- Keep notes short and practical.
`;

    const response = await client.responses.create({
      model: process.env.OPENAI_MODEL || "gpt-5.6-luna",
      input: [
        {
          role: "user",
          content: [
            { type: "input_text", text: prompt },
            {
              type: "input_image",
              image_url: `data:${mimeType};base64,${cleanedBase64}`
            }
          ]
        }
      ]
    });

    const raw = response.output_text?.trim();

    if (!raw) {
      return res.status(502).json({
        ok: false,
        error: "Empty response from AI service."
      });
    }

    let result;
    try {
      result = JSON.parse(raw);
    } catch {
      const match = raw.match(/\{[\s\S]*\}/);
      if (!match) {
        return res.status(502).json({
          ok: false,
          error: "AI response was not valid JSON.",
          raw
        });
      }
      result = JSON.parse(match[0]);
    }

    return res.json({
      ok: true,
      result
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      ok: false,
      error: error?.message || "Server error."
    });
  }
});

app.listen(port, "0.0.0.0", () => {
  console.log(`AI Kitchen Scale server listening on port ${port}`);
});
