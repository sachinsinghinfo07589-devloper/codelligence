import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import { z } from 'zod';

const app = express();
const PORT = process.env.PORT || 4000;

// Security Middleware
app.use(helmet());
app.use(cors({ origin: process.env.FRONTEND_URL || '*' }));
app.use(express.json({ limit: '1mb' }));

// Rate Limiting (Indian Intermediary & Fair-Use Policy)
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 20,
  message: { success: false, error: 'Too many authentication attempts. Please try again later.' }
});

const postingLimiter = rateLimit({
  windowMs: 60 * 60 * 1000,
  max: 10,
  message: { success: false, error: 'Hourly fair-use posting limit reached. Please wait before submitting more listings.' }
});

// Zod Validation Schemas
const RegisterSchema = z.object({
  name: z.string().min(2),
  email: z.string().email(),
  password: z.string().min(8)
});

const ListingCreateSchema = z.object({
  businessId: z.string(),
  title: z.string().min(5).max(120),
  description: z.string().min(20).max(3000),
  category: z.string(),
  listingType: z.enum(['BUSINESS', 'PRODUCT', 'SERVICE', 'EVENT', 'COURSE', 'INTERNSHIP']),
  destinationUrl: z.string().url().optional().or(z.literal('')),
  city: z.string().optional(),
  state: z.string().optional(),
  isOnlineAvailable: z.boolean().default(true),
  contactEmail: z.string().email().optional().or(z.literal('')),
  contactPhone: z.string().optional().or(z.literal(''))
});

// Health check
app.get('/health', (req: Request, res: Response) => {
  res.json({
    status: 'healthy',
    platform: 'Codelligence Free Ads Provider',
    owner: 'Codelligence Technologies',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

// 1. Auth Endpoints
app.post('/api/v1/auth/register', authLimiter, (req: Request, res: Response) => {
  const result = RegisterSchema.safeParse(req.body);
  if (!result.success) {
    return res.status(400).json({ success: false, errors: result.error.errors });
  }
  return res.status(201).json({
    success: true,
    token: 'jwt_mock_token_sample',
    refreshToken: 'refresh_mock_token_sample',
    user: {
      id: 'usr_new_01',
      name: result.data.name,
      email: result.data.email,
      role: 'USER'
    }
  });
});

app.post('/api/v1/auth/login', authLimiter, (req: Request, res: Response) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ success: false, error: 'Email and password required' });
  }
  return res.json({
    success: true,
    token: 'jwt_mock_token_sample',
    refreshToken: 'refresh_mock_token_sample',
    user: {
      id: 'usr_01',
      name: 'Aman Sharma',
      email: email,
      role: 'BUSINESS_OWNER'
    }
  });
});

// 2. Listings Endpoints
app.get('/api/v1/listings', (req: Request, res: Response) => {
  const { category, search, type, city, page = 1, limit = 20 } = req.query;
  // In production, queries Prisma: prisma.listing.findMany({ where: { status: 'APPROVED', expiresAt: { gt: new Date() } } })
  res.json({
    success: true,
    message: 'Active approved listings retrieved successfully',
    data: [],
    page: Number(page),
    limit: Number(limit)
  });
});

app.post('/api/v1/listings', postingLimiter, (req: Request, res: Response) => {
  const result = ListingCreateSchema.safeParse(req.body);
  if (!result.success) {
    return res.status(400).json({ success: false, errors: result.error.errors });
  }
  res.status(201).json({
    success: true,
    message: 'Listing submitted for review (₹0 fee applied). Status: PENDING_REVIEW',
    data: {
      ...result.data,
      id: 'lst_' + Math.random().toString(36).substring(7),
      status: 'PENDING_REVIEW',
      validityDays: 60,
      createdAt: new Date().toISOString()
    }
  });
});

// 3. Admin Moderation Endpoints
app.get('/api/v1/admin/listings/pending', (req: Request, res: Response) => {
  res.json({
    success: true,
    data: []
  });
});

app.post('/api/v1/admin/listings/:id/approve', (req: Request, res: Response) => {
  const { id } = req.params;
  res.json({
    success: true,
    message: `Listing ${id} approved and published to live public feed.`
  });
});

app.post('/api/v1/admin/listings/:id/reject', (req: Request, res: Response) => {
  const { id } = req.params;
  const { reason } = req.body;
  if (!reason) {
    return res.status(400).json({ success: false, error: 'Rejection reason is mandatory.' });
  }
  res.json({
    success: true,
    message: `Listing ${id} rejected with reason: ${reason}`
  });
});

app.listen(PORT, () => {
  console.log(`[Codelligence Technologies] Free Ads Provider API running on port ${PORT}`);
});
