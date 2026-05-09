require("dotenv").config();
const express = require("express");

const app = express();
app.use(express.json());

const PORT = process.env.PORT || 3004;

// Health check
app.get("/health", (req, res) => {
    res.status(200).json({ service: "audit-service", status: "ok" });
});

// Basic audit endpoint
app.post("/api/v1/audit/events", (req, res) => {
    const { action, actor, details } = req.body;

    if (!action || !actor) {
        return res.status(400).json({
            error: "action and actor are required"
        });
    }

    // TODO: persist to DB / publish to Kafka later
    console.log("Audit event:", { action, actor, details });

    return res.status(201).json({
        message: "Audit event recorded"
    });
});

app.listen(PORT, () => {
    console.log(`audit-service running on port ${PORT}`);
});
