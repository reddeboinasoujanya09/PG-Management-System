require("dotenv").config();
const express = require("express");

const app = express();
app.use(express.json());

const PORT = process.env.PORT || 3002;

// Health check
app.get("/health", (req, res) => {
    res.status(200).json({ service: "notification-service", status: "ok" });
});

// Matches API Gateway path: /api/v1/notifications/**
app.post("/api/v1/notifications/send", (req, res) => {
    const { to, subject, message } = req.body;

    if (!to || !subject || !message) {
        return res.status(400).json({
            error: "to, subject, and message are required"
        });
    }

    // TODO: plug in nodemailer / kafka later
    console.log("Notification request:", { to, subject, message });

    return res.status(202).json({
        message: "Notification accepted"
    });
});

app.listen(PORT, () => {
    console.log(`notification-service running on port ${PORT}`);
});
