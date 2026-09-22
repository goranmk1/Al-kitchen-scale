# AI Kitchen Scale Server

Railway-ready Node.js backend for the AI Kitchen Scale app.

## Files

- `server.js` — API server
- `package.json` — dependencies/start command
- `Dockerfile` — Railway/Docker deployment
- `.env.example` — environment variable names only

## Railway setup

1. Create or open the Railway project.
2. Connect the GitHub repository that contains these server files.
3. In Railway → Variables add:
   - `OPENAI_API_KEY` = your OpenAI API key
   - optional: `OPENAI_MODEL` = `gpt-5.6-luna`
4. Deploy.
5. Copy the public Railway HTTPS URL.

Do **not** put `OPENAI_API_KEY` in GitHub, in the app source, or in chat.

## API

### `GET /health`

Returns:

```json
{ "ok": true }
```

### `POST /analyze`

Request body:

```json
{
  "imageBase64": "BASE64_IMAGE_DATA",
  "mimeType": "image/jpeg",
  "language": "mk"
}
```

Response shape:

```json
{
  "ok": true,
  "result": {
    "food_name": "...",
    "estimated_weight_g": 0,
    "estimated_calories_kcal": 0,
    "protein_g": 0,
    "carbs_g": 0,
    "fat_g": 0,
    "confidence": 0,
    "notes": "..."
  }
}
```

## Android app connection

Set the app's `ANALYSIS_URL` to the Railway public HTTPS endpoint:

`https://YOUR-RAILWAY-DOMAIN/analyze`

Then rebuild the APK.

## Important

The estimated weight comes from visual AI analysis. It is not a physical weighing-scale measurement.
